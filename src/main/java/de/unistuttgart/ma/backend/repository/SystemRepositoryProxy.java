package de.unistuttgart.ma.backend.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unistuttgart.ma.saga.System;

/**
 * 
 * 
 * 
 * @author maumau
 *
 */
@Component
public class SystemRepositoryProxy {

	private final SystemRepository repository;
	private final ResourceSet set;

	public SystemRepositoryProxy(@Autowired SystemRepository repository, @Autowired ResourceSet set) {
		this.repository = repository;
		this.set = set;

		this.loadedSystems = new HashMap<>();
		this.projectId2SystemId = new HashMap<>();
		this.systemId2ResourceUri = new HashMap<>();

		// load maps from data base.
	}

	// TODO DELETE
	public void testInit() throws IOException {
//		String xml = Files.readString(Paths.get("src/test/resources/", "t2_base.saga"), StandardCharsets.UTF_8);
//		// URI uri = URI.createFileURI("fiii/Myqwer.saga");
//		URI uri = URI.createFileURI("test/test.saga");
//		String filename = uri.path();
//		String id = "60fa9cadc736ff6357a89a9b";
//
//		SystemItem item = new SystemItem(id, xml, filename);
//
//		repository.save(item);
//
//		systemId2ResourceUri.put(item.getId(), item.getFilename());
//		projectId2SystemId.put("5e8cc17ed645a00c", id);
	}

	// systemItemid -> system
	private final Map<String, de.unistuttgart.ma.saga.System> loadedSystems;

	// TODO : 1 : n mapping
	private final Map<String, String> projectId2SystemId;

	// systemID -> FileName
	private final Map<String, String> systemId2ResourceUri;

	/**
	 * save a system to the repository.
	 * 
	 * systems are serialised to xml and that xml is saved in the database.
	 * 
	 * @param system the system to be saved
	 * @throws IOException if the model could not be saved
	 */
	public String save(de.unistuttgart.ma.saga.System system) throws IOException {
		if (system.eResource() == null) {
			throw new IllegalArgumentException("system is not contained in any reource.");
		}
		if (system.getId() == null) {
			String id = getIdForSystem(system);
			system.setId(id);
		}
		if (!repository.existsById(system.getId())) {
			SystemItem item = repository.save(new SystemItem(system.getId(), null, system.getName()));
			systemId2ResourceUri.put(item.getId(), item.getFilename());
		}
			
		SystemItem item = repository.findById(system.getId()).get();

		repository.save(new SystemItem(item.getId(), serializeSystem(system), item.getFilename()));
		projectId2SystemId.put(system.getArchitecture().getId(), system.getId());
		
		return item.getId();
	}
	
	/**
	 * 
	 * @param system system with unset id
	 * @return
	 * @throws IOException
	 */
	public String getIdForSystem(System system) throws IOException {
		SystemItem item = repository.save(new SystemItem(null, null, system.getName()));
		systemId2ResourceUri.put(item.getId(), item.getFilename());
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
		if (loadedSystems.containsKey(id)) {
			return loadedSystems.get(id);
		}
		if (repository.existsById(id)) {
			SystemItem item = repository.findById(id).get();
			try {
				System system = deserializeSystem(item.getContent(), item.getFilename());
				loadedSystems.put(item.getId(), system);
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
	 * reserve an entry in the database for the model. 
	 * 
	 * @param fileuri uri of the file at the frontend. 
	 * @return an id
	 */
	public String getIdForFilename(String fileuri) {
		SystemItem item = new SystemItem(null, null, fileuri);
		return repository.save(item).getId();
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
