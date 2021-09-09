package de.unistuttgart.ma.backend.repository;

import org.springframework.data.annotation.Id;

import de.unistuttgart.ma.impact.Impact;

/**
 * An {@code ImpactItem} is an item to be saved in the {@link ImpactRepository}.
 * 
 * It consists of the impact location, its id and the id of its cause. The ids
 * come from the database to ensure that they are unique.
 * 
 * In fact the unique ids are them main reason for persisting the impact at all.
 *
 */
public class ImpactItem {
	@Id
	private String id;
	private String cause;
	private String location;

	public ImpactItem() {
	}

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
		this.location = impact.getLocationId();

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
