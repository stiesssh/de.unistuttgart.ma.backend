package de.unistuttgart.ma.backend;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.springframework.beans.factory.annotation.Autowired;

import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.ma.backend.computationUtility.QueueItem;
import de.unistuttgart.ma.backend.repository.ImpactRepositoryProxy;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.saga.Saga;
import de.unistuttgart.ma.saga.SagaStep;
import de.unistuttgart.ma.saga.System;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * calculates impact after receiving alert about and violation
 * 
 * @author maumau
 *
 */
@org.springframework.stereotype.Component
public class NotificationCreationService {

	private final ImpactRepositoryProxy notificationRepoProxy;
	private final SystemRepositoryProxy systemRepoProxy;

	public NotificationCreationService(@Autowired ImpactRepositoryProxy notificationRepoProxy,
			@Autowired SystemRepositoryProxy systemRepoProxy) {
		this.notificationRepoProxy = notificationRepoProxy;
		this.systemRepoProxy = systemRepoProxy;
	}

	/**
	 * 
	 * Compute the impact of a violation and store it in the repository
	 * 
	 * @param violation reported violation
	 */
	public Set<Notification> calculateImpacts(Violation violation) {

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
			Impact causedImpact = ImpactFactory.eINSTANCE.createImpact();
			causedImpact.setCause(currentItem.getCause());
			causedImpact.setLocation(current);
			//causedImpact.setId(); // TODO 
			

			Set<SagaStep> nextSteps = getNextLevel(current, system);
			if (nextSteps.isEmpty()) {
				// traverse architecture
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
			Impact causedImpact = ImpactFactory.eINSTANCE.createImpact();

			causedImpact.setLocation(current);
			causedImpact.setCause(currentItem.getCause());
			

			Impact topLevelImpact = ImpactFactory.eINSTANCE.createImpact();
			topLevelImpact.setLocation(current.getTask());
			topLevelImpact.setCause(causedImpact);

			//notificationRepoProxy.save(topLevelImpact, system.getId());
			Notification note = ImpactFactory.eINSTANCE.createNotification();
			//note.setId(value);
			note.setRootCause(violation);
			note.setTopLevelImpact(topLevelImpact);
			notes.add(note);
		}
		return notes;
	}

	protected Set<SagaStep> getNextLevel(ComponentInterface face, System system) {
		Set<SagaStep> nexts = new HashSet<>();

		// TODO : are these really the same objects, or do the just 'look' the same??
		// TODO : maybe compare by id or override equals.
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
	 * Create the initial Items for the Queue.
	 *
	 * Violations may happen at a Component as well as an Interface. However the
	 * impact computation operates on Interfaces. As such in case of a violation at
	 * a component, the initial items are that components provided interfaces. With
	 * 'cause' being the initial violation. In case the violation happens at an
	 * interface, the initial violations are the provided interfaces of the
	 * consuming components, or else i would 'loose' the actual violation.
	 * 
	 * (because queueItem = previous impact x (small) location, but if location is
	 * location of actual violation, then previous impact is null and everything
	 * breaks...)
	 * 
	 * @param violation
	 * @return
	 */
	private Set<QueueItem> makeInitialItems(Violation violation) {

		Set<QueueItem> initialItems = new HashSet<>();

		// "finer grain" (interface is set)
		if (violation.getViolatedRule().getGropiusComponentInterface() != null) {
			Set<Component> impactedComponents = new HashSet<>();
			impactedComponents.addAll(violation.getViolatedRule().getGropiusComponentInterface().getConsumedBy());
			
			Impact initialImpact = ImpactFactory.eINSTANCE.createImpact();
			initialImpact.setCause(null);
			initialImpact.setLocation(violation.getViolatedRule().getGropiusComponentInterface());
			
			for (Component c : impactedComponents) {
				for (ComponentInterface face : c.getInterfaces()) {
					initialItems.add(new QueueItem(initialImpact, face));
				}
			}

			// "coarser grain" (interface not set, violation aggregated at component)
		} else if (violation.getViolatedRule().getGropiusComponent() != null) {
			EList<ComponentInterface> faces = violation.getViolatedRule().getGropiusComponent().getInterfaces();
			for (ComponentInterface componentInterface : faces) {
				Impact initialImpact = ImpactFactory.eINSTANCE.createImpact();
				initialImpact.setCause(null);
				initialImpact.setLocation(componentInterface);
				
				for (Component c : componentInterface.getConsumedBy()) {
					for (ComponentInterface face : c.getInterfaces()) {
						initialItems.add(new QueueItem(initialImpact, face));
					}
				}
			}
		} else {
			throw new IllegalArgumentException("the given violation does not have a location");
		}

		
		

		return initialItems;
	}
}
