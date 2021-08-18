package de.unistuttgart.ma.backend.repository;

import org.eclipse.bpmn2.Task;
import org.springframework.data.annotation.Id;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.saga.SagaStep;

/**
 * Item to be saved in the Impact DataBase.
 * 
 * Content is the actual impact, serialised into ecore xml format. 
 * 
 * @author maumau
 *
 */
public class ImpactItem {
	@Id
	private String id;
	private String cause;
	private String location;
	
	public ImpactItem() {	}
	
	public ImpactItem(String id, String cause, String location) {
		super();
		this.id = id;
		this.cause = cause;
		this.location = location;
	}

	public ImpactItem(Impact impact) {
		super();
		this.id = null;
		if (impact.getCause() != null) {
			this.cause = impact.getCause().getId();
		}
		if (impact.getLocation() instanceof ComponentInterface) {
			this.location = ((ComponentInterface) impact.getLocation()).getId();
		} else 	if (impact.getLocation() instanceof Task) {
			this.location = ((Task) impact.getLocation()).getId();
		} if (impact.getLocation() instanceof SagaStep) {
			this.location = ((SagaStep) impact.getLocation()).getId();
		}
	}

	public String getId() {
		return id;
	}
	public String getCause() {
		return cause;
	}
	public String getLocation() {
		return location;
	}	
}
