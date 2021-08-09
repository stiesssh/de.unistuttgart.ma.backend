package de.unistuttgart.ma.backend.rest;

/**
 * 
 * @author maumau
 *
 */
public class ImportRequest {
	
	private final String solomonUrl;
	private final String gropiusUrl;
	private final String bpmnUrl;
	
	private final String gropiusProjectId;
	private final String solomonEnvironment;
	
	private final String ressourceUri;
	
	public ImportRequest(String solomonUrl, String gropiusUrl, String bpmnUrl, String gropiusProjectId,
			String solomonEnvironment, String ressourceUri) {
		super();
		this.solomonUrl = solomonUrl;
		this.gropiusUrl = gropiusUrl;
		this.bpmnUrl = bpmnUrl;
		this.gropiusProjectId = gropiusProjectId;
		this.solomonEnvironment = solomonEnvironment;
		this.ressourceUri = ressourceUri;
	}

	public String getSolomonUrl() {
		return solomonUrl;
	}
	public String getGropiusUrl() {
		return gropiusUrl;
	}
	public String getBpmnUrl() {
		return bpmnUrl;
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
}
