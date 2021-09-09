package de.unistuttgart.ma.backend.app;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.eclipse.bpmn2.Task;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;

import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.ma.backend.repository.ImpactItem;
import de.unistuttgart.ma.backend.repository.ImpactRepository;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.saga.Saga;
import de.unistuttgart.ma.saga.SagaStep;
import de.unistuttgart.ma.saga.System;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * Responsible for calculating the impacts of a violation.
 * 
 * @author maumau
 *
 */
@org.springframework.stereotype.Component
public class CalculateNotificationService {

	private final SystemRepositoryProxy systemRepoProxy;
	private final ImpactRepository impactRepo;

	public CalculateNotificationService(@Autowired SystemRepositoryProxy systemRepoProxy,
			@Autowired ImpactRepository impactRepo) {
		assert (systemRepoProxy != null && impactRepo != null);
		this.systemRepoProxy = systemRepoProxy;
		this.impactRepo = impactRepo;
	}

	/**
	 * Calculate the impacts of a violation.
	 * 
	 * Hops through the model of the system to trace how the violation might
	 * propagate. Each trace is calculated as a chain of impacts. Each impact that
	 * reaches the business process is wrapped into a notification and returned.
	 * 
	 * @param violation violation to calculate impacts for
	 * @return notification for each impact chain that reaches the business process
	 */
	public Set<Notification> calculateImpacts(Violation violation) {
		if (violation == null) {
			throw new IllegalArgumentException("violation must not be null");
		}

		Set<Notification> notes = new HashSet<Notification>();

		String architectureId = violation.getViolatedRule().getGropiusProject().getId();
		System system = systemRepoProxy.findByArchitectureId(architectureId);

		Queue<QueueItem> queue = new LinkedList<QueueItem>();
		queue.addAll(makeInitialItems(violation));

		Queue<QueueItem> sagaqueue = new LinkedList<QueueItem>();

		// go along architecture
		while (!queue.isEmpty()) {
			QueueItem currentItem = queue.remove();
			ComponentInterface current = currentItem.getLocationAsFace();

			// always cause new impact at current
			Impact causedImpact = makeImpact(currentItem.getCause(), current);

			Set<SagaStep> nextSteps = getNextLevel(current, system);
			if (nextSteps.isEmpty()) {
				// stay at architecture
				EList<Component> consumers = current.getConsumedBy();
				for (Component component : consumers) {
					for (ComponentInterface provided : component.getInterfaces()) {
						queue.add(new QueueItem(causedImpact, provided));
					}
				}
			} else {
				// switch to saga
				for (SagaStep step : nextSteps) {
					sagaqueue.add(new QueueItem(causedImpact, step));
				}
			}
		}

		// do saga
		while (!sagaqueue.isEmpty()) {
			QueueItem currentItem = sagaqueue.remove();
			SagaStep current = currentItem.getLocationAsStep();

			// always cause new impact at current
			Impact causedImpact = makeImpact(currentItem.getCause(), current);

			Impact topLevelImpact = makeImpact(causedImpact, current.getTask());

			Notification note = ImpactFactory.eINSTANCE.createNotification();
			note.setRootCause(violation);
			note.setTopLevelImpact(topLevelImpact);
			notes.add(note);
		}
		return notes;
	}

	/**
	 * Get the saga steps realized with the given component interface.
	 * 
	 * Get the saga steps by looking them up in the system model. The given
	 * interface must be part of the system model.
	 * 
	 * @param face   the interface whose steps to get
	 * @param system model of the system
	 * @return set of saga steps realized with face.
	 */
	protected Set<SagaStep> getNextLevel(ComponentInterface face, System system) {
		assert (face != null && system != null);
		if (!system.getComponentInterfaceById(face.getId()).equals(face)) {
			throw new IllegalArgumentException(String.format("Interface %s does not belong to system %s", face.getId(), system.getId()));
		}

		Set<SagaStep> nexts = new HashSet<>();

		for (Saga saga : system.getSagas()) {
			for (SagaStep sagaStep : saga.getSteps()) {
				if (sagaStep.getComponentInterface().equals(face)) {
					nexts.add(sagaStep);
				}
			}
		}
		return nexts;
	}

	/**
	 * 
	 * Create queue items for the initial impacts.
	 *
	 * A violation may happen at a Component as well as at an Interface. However the
	 * impact computation operates on Interfaces. In case of a violation at a
	 * component, the initial items are the interfaces provided by that component.
	 * 
	 * (because queueItem = previous impact x (small) location, but if location is
	 * location of actual violation, then previous impact is null and everything
	 * breaks...)
	 * 
	 * @param violation the violation
	 * @return queue items for the initial impacts
	 */
	private Set<QueueItem> makeInitialItems(Violation violation) {

		Set<QueueItem> initialItems = new HashSet<>();

		// "finer grain" (interface is set) i
		if (violation.getViolatedRule().getGropiusComponentInterface() != null) {
			initialItems.add(new QueueItem(null, violation.getViolatedRule().getGropiusComponentInterface()));
//			Set<Component> impactedComponents = new HashSet<>();
//			impactedComponents.addAll(violation.getViolatedRule().getGropiusComponentInterface().getConsumedBy());

//			Impact initialImpact = makeImpact(null, violation.getViolatedRule().getGropiusComponentInterface());
			
//			for (Component c : impactedComponents) {
//				for (ComponentInterface face : c.getInterfaces()) {
//					initialItems.add(new QueueItem(initialImpact, face));
//				}
//			}

			// "coarser grain" (interface not set, violation aggregated at component)
		} else if (violation.getViolatedRule().getGropiusComponent() != null) {
			EList<ComponentInterface> faces = violation.getViolatedRule().getGropiusComponent().getInterfaces();
			for (ComponentInterface componentInterface : faces) {
				initialItems.add(new QueueItem(null, componentInterface));

//				Impact initialImpact = makeImpact(null, componentInterface);
//				for (Component c : componentInterface.getConsumedBy()) {
//					for (ComponentInterface face : c.getInterfaces()) {
//						initialItems.add(new QueueItem(initialImpact, face));
//					}
//				}
			}
		} else {
			throw new IllegalArgumentException("the given violation does not have a location");
		}

		return initialItems;
	}

	/**
	 * Creates a new impact and saves it to the impact repository.
	 * 
	 * @param cause    cause of the impact
	 * @param location location of the impact
	 * @return new impact, that is also saved to the impact repository.
	 */
	private Impact makeImpact(Impact cause, EObject location) {
		assert (location != null);
		Impact causedImpact = ImpactFactory.eINSTANCE.createImpact();
		causedImpact.setCause(cause);
		causedImpact.setLocation(location);

		ImpactItem item = impactRepo.save(new ImpactItem(causedImpact));

		causedImpact.setId(item.getId());

		return causedImpact;
	}
	
	/**
	 * The sole reason of existence of this class is, that the impact calculation at
	 * the CoreService happens in the opposite direction to the linking of the
	 * impact chain and thus at each iteration of the calculation the precious
	 * impact must be known. and the easiest ways to achieve this seemed to also put
	 * it into the queue.
	 */
	private class QueueItem {
		public final Impact cause;
		public final EObject location;
		
		public QueueItem(Impact cause, EObject location) {
			super();
			this.cause = cause;
			this.location = location;
		}
		
		public Impact getCause() {
			return cause;
		}
		public ComponentInterface getLocationAsFace() {
			return (ComponentInterface) location;
		}
		public SagaStep getLocationAsStep() {
			return (SagaStep) location;
		}
		public Task getLocationAsTask() {
			return (Task) location;
		}
	}

}
