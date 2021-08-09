package de.unistuttgart.ma.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unistuttgart.ma.backend.repository.ImpactRepositoryProxy;
import de.unistuttgart.ma.saga.impact.Impact;
import de.unistuttgart.ma.saga.impact.ImpactFactory;
import de.unistuttgart.ma.saga.impact.Notification;

/**
 * Responsible for retrieving Notifications.
 * 
 * @author maumau
 *
 */
@Component
public class NotificationRetrievalService {

	private final ImpactRepositoryProxy repository;
	private final ResourceSet set;

	public NotificationRetrievalService(@Autowired ImpactRepositoryProxy repository, @Autowired ResourceSet set) {
		this.repository = repository;
		this.set = set;
	}

	/**
	 * Retrieves the serialised notification for the system with the given id.
	 * 
	 * Gets the all impacts for the system with the given id, wrap them into a
	 * notification, serialise that notification and return the resulting xml.
	 * 
	 * @param systemId id of system to get the notification for
	 * @return notification for system with given id
	 * @throws IOExceptio if any de/serialisation failed.
	 */
	public String retrieveNotification(String systemId) throws IOException {
		Set<Impact> impacts = repository.findBySystemId(systemId);
		Notification note = makeContainerNotification(impacts);

		Resource res = set.createResource(URI.createPlatformPluginURI("foo.impact", false));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		res.getContents().add(note);
		res.save(outputStream, null);

		return outputStream.toString();
	}

	/**
	 * Create a new notification that contains the given impacts.
	 * 
	 * The impacts are a linked list, but also each impact must directly be
	 * contained within the notification, or else it will get lost.
	 * 
	 * @param impacts the impacts
	 * @return notifications that contains all given impacts
	 */
	private Notification makeContainerNotification(Set<Impact> impacts) {
		Notification note = ImpactFactory.eINSTANCE.createNotification();
		note.getTopLevelImpacts().addAll(impacts);

		for (Impact impact : impacts) {
			Impact current = impact;
			while (current != null) {
				note.getImpacts().add(current);
				current = current.getCause();
			}
		}
		return note;
	}
}
