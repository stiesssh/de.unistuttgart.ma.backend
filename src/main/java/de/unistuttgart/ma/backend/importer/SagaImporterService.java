package de.unistuttgart.ma.backend.importer;

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
 * responsible for parsing input to a system instance and save that instance to
 * the repository
 * 
 * @author maumau
 *
 */
@Component
public class SagaImporterService {

	private final SystemRepositoryProxy systemRepoProxy;
	private final ResourceSet set;

	public SagaImporterService(@Autowired SystemRepositoryProxy systemRepoProxy, @Autowired ResourceSet set) {
		this.systemRepoProxy = systemRepoProxy;
		this.set = set;

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("saga", new EcoreResourceFactoryImpl());
		SagaPackage packageInstance = SagaPackage.eINSTANCE;
	}

	public String getIdForSystemModel(String filename) {
		return systemRepoProxy.getIdForFilename(filename);
	}

	public String createModel(ImportRequest request) throws IOException, ModelCreationFailedException {
		// collect model elements with importers
		Project arch = getArchitecture(request.getGropiusUrl(), request.getGropiusProjectId());
		Set<SloRule> rules = getSloRules(request.getSolomonUrl(), request.getSolomonEnvironment());
		Process process = getProcess(request.getBpmn());

		// merge them
		System system = SagaFactory.eINSTANCE.createSystem();
		system.setArchitecture(arch);
		system.getProcesses().add(process);
		system.getSloRules().addAll(rules);
		system.setName(request.getRessourceUri());

		// set the resource
		Resource res = set.createResource(URI.createPlatformResourceURI(request.getRessourceUri(), false));
		res.getContents().add(system);

		// save to db and return
		String id = systemRepoProxy.save(system);
		return systemRepoProxy.findXMLById(id);
	}

	
	/**
	 * 
	 * @param xml
	 * @param systemId
	 */
	public void updateModel(String xml, String systemId) {
		systemRepoProxy.updateModel(xml, systemId);
	}
	
	/**
	 * import SLO rules.
	 * 
	 * @param url
	 * @param env
	 * @return a set of Slo rules.
	 * @throws ModelCreationFailedException 
	 */
	protected Set<SloRule> getSloRules(String url, String env) throws ModelCreationFailedException {
		return new SolomonImporter(url, env).parse();
	}

	/**
	 * import components and interfaces from gropius.
	 * 
	 * @param url
	 * @param projectId
	 * @return components and their interfaces
	 * @throws ModelCreationFailedException 
	 */
	protected Project getArchitecture(String url, String projectId) throws ModelCreationFailedException {
		return new GropiusImporter(url, projectId).parse();
	}

	/**
	 * import bpmn process.
	 * 
	 * @param url
	 * @return a bpmn process
	 */
	protected Process getProcess(String bpmn) throws ModelCreationFailedException {
		return new BPMNImporter(bpmn).parse();
	}

}
