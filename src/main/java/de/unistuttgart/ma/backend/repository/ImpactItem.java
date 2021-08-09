package de.unistuttgart.ma.backend.repository;

import org.springframework.data.annotation.Id;

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
	private final String id;
	private final String systemId;
	private final String content;
	
	public ImpactItem(String id, String systemId, String content) {
		super();
		this.id = id;
		this.systemId = systemId;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public String getSystemId() {
		return systemId;
	}

	public String getContent() {
		return content;
	}
}
