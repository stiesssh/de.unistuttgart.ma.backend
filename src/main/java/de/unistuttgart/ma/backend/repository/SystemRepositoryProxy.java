package de.unistuttgart.ma.backend.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unistuttgart.ma.saga.SagaPackage;
import de.unistuttgart.ma.saga.System;

/**
 * A {@code SystemRepositoryProxy} is a Proxy to the {@link SystemRepository}.
 * 
 * It translates the {@link System}s to {@link SystemItem}s to save them in the
 * repository and vice versa. All acces to the repository should happen through
 * this proxy.
 * 
 * @author maumau
 *
 */
@Component
public class SystemRepositoryProxy {

	private final SystemRepository repository;
	private final ResourceSet set;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public SystemRepositoryProxy(@Autowired SystemRepository repository, @Autowired ResourceSet set) {
		assert (repository != null && set != null);
		this.repository = repository;
		this.set = set;

		this.projectId2SystemId = new HashMap<>();
		//this.systemId2ResourceUri = new HashMap<>();

		SagaPackage e = SagaPackage.eINSTANCE;

		init();
	}

	/**
	 * Load the content of the {@code systemId2ResourceUri} and of the
	 * {@code projectId2SystemId} mappings from the repository.
	 */
	public void init() {
		List<SystemItem> items = repository.findAll();
		logger.info(String.format("loading %d entries from database.", items.size()));
		for (SystemItem item : items) {
			try {
				System system = deserializeSystem(item.getContent(), item.getFilename());
				projectId2SystemId.put(system.getArchitecture().getId(), system.getId());
				//systemId2ResourceUri.put(system.getId(), item.getFilename());
				logger.info(String.format("load model %s for architecture %s.", system.getId(),
						system.getArchitecture().getId()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * maps ids of gropius projects to the ids of the system models, because alerts
	 * only know the gropius project but the impact calculation needs the model of
	 * the entire system
	 */
	private final Map<String, String> projectId2SystemId;

	/** TODO : why does this exist?? */
	//private final Map<String, String> systemId2ResourceUri;

	/**
	 * Save a system model to the repository.
	 * 
	 * The model is serialised to xml with the ecore utilities and that xml is saved to the repository.
	 * 
	 * @param system the system model to be saved
	 * @throws IOException if the model could not be saved
	 */
	public String save(de.unistuttgart.ma.saga.System system) throws IOException {
		if (system == null) {
			throw new IllegalArgumentException("system is null.");
		}
		if (system.eResource() == null) {
			throw new IllegalArgumentException("system is not contained in any reource.");
		}
		if (system.getId() == null) {
			String id = getIdForSystem(system);
			system.setId(id);
		}
		if (!repository.existsById(system.getId())) {
			SystemItem item = repository.save(new SystemItem(system.getId(), null, system.getName()));
			//systemId2ResourceUri.put(item.getId(), item.getFilename());
		}

		SystemItem item = repository.findById(system.getId()).get();

		repository.save(new SystemItem(item.getId(), serializeSystem(system), item.getFilename()));
		projectId2SystemId.put(system.getArchitecture().getId(), system.getId());

		return item.getId();
	}

	/**
	 * update model by replacing its xml with another one.
	 * 
	 * if there is no model matching the given systemId, the model is saved to the
	 * db as a new entry.
	 * 
	 * @param xml      the model as xml
	 * @param systemId id of the system in the model
	 */
	public void updateModel(String xml, String systemId) {
		if (repository.existsById(systemId)) {
			SystemItem item = repository.findById(systemId).get();
			repository.save(new SystemItem(systemId, xml, item.getFilename()));
			logger.info(String.format("updated model %s", systemId));
		} else {
			repository.save(new SystemItem(systemId, xml, systemId + ".saga"));
			logger.info(String.format("no model %s, saved as new model", systemId));
		}
	}

	/**
	 * 
	 * @param system system with unset id
	 * @return
	 * @throws IOException
	 */
	public String getIdForSystem(System system) throws IOException {
		SystemItem item = repository.save(new SystemItem(null, null, system.getName()));
		//systemId2ResourceUri.put(item.getId(), item.getFilename());
		return item.getId();
	}

	/**
	 * Find the System that imports the architecture with the given project id.
	 * 
	 * @param projectId id of a gropius project.
	 * @return system that import the gropius architecture with the given projectId
	 */
	public System findByArchitectureId(String projectId) {
		return findById(projectId2SystemId.get(projectId));
	}

	/**
	 * Find the System with the given id.
	 * 
	 * @param id id of the system to be retrieved
	 * @return the system with the given id
	 */
	public System findById(String id) {
		if (repository.existsById(id)) {
			SystemItem item = repository.findById(id).get();
			try {
				System system = deserializeSystem(item.getContent(), item.getFilename());
				return system;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new NoSuchElementException(String.format("Missing System for id %s", id));
	}

	/**
	 * Find the System with the given id.
	 * 
	 * @param id id of the system to be retrieved
	 * @return the system with the given id as xml
	 */
	public String findXMLById(String id) {
		if (repository.existsById(id)) {
			SystemItem item = repository.findById(id).get();
			return item.getContent();
		}
		throw new NoSuchElementException(String.format("Missing System for id %s", id));
	}

	/**
	 * Serialise system to ecore xml.
	 * 
	 * @param system
	 * @return ecore xml representation of impact.
	 * @throws IOException
	 */
	protected String serializeSystem(System system) throws IOException {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("saga", new EcoreResourceFactoryImpl());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Resource res = system.eResource();
		res.save(outputStream, null);

		return outputStream.toString(StandardCharsets.UTF_8);
	}

	/**
	 * Deserialise system from ecore xml.
	 * 
	 * @param impact
	 * @return ecore xml representation of impact.
	 * @throws IOException
	 */
	protected System deserializeSystem(String xml, String filename) throws IOException {
		assert(xml != null && filename != null);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("saga", new EcoreResourceFactoryImpl());
		InputStream targetStream = new ByteArrayInputStream(xml.getBytes());

		Resource resource = set.getResource(URI.createPlatformResourceURI(filename, false), false);
		if (resource == null) {
			resource = set.createResource(URI.createPlatformResourceURI(filename, false));
		}
		resource.load(targetStream, null);

		for (EObject eObject : resource.getContents()) {
			if (eObject instanceof System) {
				return (System) eObject;
			}
		}
		throw new IOException("Could not deserialize " + xml);
	}
}
