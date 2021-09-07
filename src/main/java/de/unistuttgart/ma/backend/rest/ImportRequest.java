package de.unistuttgart.ma.backend.rest;

/**
 * A {@code ImportRequest} requests the the creation of a new model. It
 * specifies which architecture, process and slo rules to import into the newly
 * created model.
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

	/**
	 * Create a new import request.
	 * 
	 * @param solomonUrl         url of the Solomon tool, to be queried for slo
	 *                           rules
	 * @param gropiusUrl         url of the Gropius back end, to be queried for the
	 *                           architecture
	 * @param gropiusProjectId   id of the gropius project to get the architecture
	 *                           from
	 * @param solomonEnvironment deployment environment to get slo rules for
	 * @param ressourceUri       ressourceUri of model at local editor. will be
	 *                           utilized as name of the model.
	 * @param bpmn               the process to import
	 */
	public ImportRequest(String solomonUrl, String gropiusUrl, String gropiusProjectId, String solomonEnvironment,
			String ressourceUri, String bpmn) {
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
