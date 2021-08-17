package de.unistuttgart.ma.backend.rest;

/**
 * 
 * @author maumau
 *
 */
public class ImportRequest {
	
	private final String solomonUrl;
	private final String gropiusUrl;
	
	private final String gropiusProjectId;
	private final String solomonEnvironment;
	
	private final String ressourceUri;
	
	private final String bpmn;
	
	public ImportRequest(String solomonUrl, String gropiusUrl, String gropiusProjectId,
			String solomonEnvironment, String ressourceUri, String bpmn) {
		super();
		this.solomonUrl = solomonUrl;
		this.gropiusUrl = gropiusUrl;
		this.gropiusProjectId = gropiusProjectId;
		this.solomonEnvironment = solomonEnvironment;
		this.ressourceUri = ressourceUri;
		this.bpmn = bpmn;
	}

	public String getSolomonUrl() {
		return solomonUrl;
	}
	public String getGropiusUrl() {
		return gropiusUrl;
	}
	public String getGropiusProjectId() {
		return gropiusProjectId;
	}
	public String getSolomonEnvironment() {
		return solomonEnvironment;
	}
	public String getRessourceUri() {
		return ressourceUri;
	}
	public String getBpmn() {
		return bpmn;
	}
}
