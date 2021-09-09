package de.unistuttgart.ma.backend.repository;

import org.springframework.data.annotation.Id;

/**
 * A {@code SystemItem} is an item to be saved in the {@link SystemRepository}.
 * 
 * Content is the actual system, serialised into ecore xml format.
 *
 */
public class SystemItem {
	@Id
	private final String id;
	private final String content;
	private final String filename;
	
	public SystemItem(String id, String content, String filename) {
		super();
		this.id = id;
		this.content = content;
		this.filename = filename;
	}

	public String getId() {
		return id;
	}
	public String getContent() {
		return content;
	}
	public String getFilename() {
		return filename;
	}
}