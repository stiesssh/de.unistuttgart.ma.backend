package de.unistuttgart.ma.backend.app;

import java.io.IOException;
import java.util.Set;

import org.eclipse.bpmn2.Process;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unistuttgart.gropius.Project;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.architecture.GropiusImporter;
import de.unistuttgart.ma.backend.importer.process.BPMNImporter;
import de.unistuttgart.ma.backend.importer.slo.SolomonImporter;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.backend.rest.ImportRequest;
import de.unistuttgart.ma.saga.SagaFactory;
import de.unistuttgart.ma.saga.SagaPackage;
import de.unistuttgart.ma.saga.System;

/**
 * Creates, updates and get models.
 */
@Component
public class ModelService {

	private final SystemRepositoryProxy systemRepoProxy;
	private final ResourceSet set;

	public ModelService(@Autowired SystemRepositoryProxy systemRepoProxy, @Autowired ResourceSet set) {
		assert (systemRepoProxy != null & set != null);
		this.systemRepoProxy = systemRepoProxy;
		this.set = set;

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("saga", new EcoreResourceFactoryImpl());

		@SuppressWarnings("unused")
		SagaPackage packageInstance = SagaPackage.eINSTANCE;
	}

	/**
	 * Get the model with the given id.
	 * 
	 * @param modelId
	 * @return xml representation of the model
	 */
	public String getModel(String modelId) {
		return systemRepoProxy.findXMLById(modelId);
	}

	/**
	 * Create new model by importing architecture, slo rules and process as
	 * specified in the import request and save it to the database.
	 * 
	 * @param request request to create a new model
	 * @return xml representation of the new model
	 * @throws ModelCreationFailedException if the creation of the model failed
	 */
	public String createModel(ImportRequest request) throws ModelCreationFailedException {

		System system = SagaFactory.eINSTANCE.createSystem();
		system.setName(request.getRessourceUri());

		// collect model elements with importers and merge into model.
		Project arch = getArchitecture(request.getGropiusUrl(), request.getGropiusProjectId());
		system.setArchitecture(arch);

		Set<SloRule> rules = getSloRules(request.getSolomonUrl(), request.getSolomonEnvironment(), system);
		system.getSloRules().addAll(rules);

		Process process = getProcess(request.getBpmn());
		system.getProcesses().add(process);

		// set the resource
		Resource res = set.createResource(URI.createPlatformResourceURI(request.getRessourceUri(), false));
		res.getContents().add(system);

		// save to db and return
		try {
			String id = systemRepoProxy.save(system);
			return systemRepoProxy.findXMLById(id);
		} catch (IOException e) {
			throw new ModelCreationFailedException("Could not save model to databas", e);
		}
	}

	/**
	 * Update the model with the given id.
	 * 
	 * @param xml     xml representation of the updated model.
	 * @param modelId id of the model to update.
	 */
	public void updateModel(String xml, String modelId) {
		systemRepoProxy.updateModel(xml, modelId);
	}

	/**
	 * Get Slo rules from solomon.
	 * 
	 * To get the rules, the architecture must already be known.
	 * 
	 * @param solomonUrl url of the solomon backend
	 * @param env        parameter that determines wether you get aws or kube rules.
	 * @param model      model of the system, must already contain the architecture.
	 * @return a set of Slo rules
	 * @throws ModelCreationFailedException if the slo rules could not be retrieved
	 */
	protected Set<SloRule> getSloRules(String solomonUrl, String env, System model)
			throws ModelCreationFailedException {
		return new SolomonImporter(solomonUrl, env, model).parse();
	}

	/**
	 * Get an architecture from gropius.
	 * 
	 * @param gropiusUrl url of the gropius backend
	 * @param projectId  id of the project to access
	 * @return a gropius project with its components and interface, i.e. the
	 *         architecture.
	 * @throws ModelCreationFailedException if the architecture could not be
	 *                                      retrieved
	 */
	protected Project getArchitecture(String gropiusUrl, String projectId) throws ModelCreationFailedException {
		return new GropiusImporter(gropiusUrl, projectId).parse();
	}

	/**
	 * Get a bpmn process.
	 * 
	 * @param bpmn xml representation of a bpmn process
	 * @return bpmn process
	 * @throws ModelCreationFailedException if the process could not be retrieved
	 */
	protected Process getProcess(String bpmn) throws ModelCreationFailedException {
		return new BPMNImporter(bpmn).parse();
	}

}
