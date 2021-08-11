package de.unistuttgart.ma.backend.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;

/**
 * Save IMPACTS!!! 
 * @author maumau
 *
 */
@Component
public class ImpactRepositoryProxy {
	
	private final ImpactRepository repository;
	private final ResourceSet set; 
	
	public ImpactRepositoryProxy(@Autowired ImpactRepository repository, @Autowired ResourceSet set) {
		this.repository = repository;
		this.set = set;
		
		this.loadedImpacts = new HashMap<>();
		this.systemId2ImpactIds = new HashMap<>();
		// systemid2impactid= // read repository;
	}
	
	Map<String, Impact> loadedImpacts;
	
	// TODO : 1 : n mapping
	Map<String, Set<String>> systemId2ImpactIds;
	
	/**
	 * save *new* impact to the database.
	 * 
	 * @param impact
	 * @param system
	 * @throws IOException 
	 */
	public void save(Impact impact, String systemId) {
		try {
			ImpactItem item = new ImpactItem(null, systemId, serializeImpact(impact));
			
			String impactId = repository.save(item).getId();

			// updated system -> impact mapping
			Set<String> impactIds;
			if (systemId2ImpactIds.containsKey(systemId)) {
				impactIds = systemId2ImpactIds.get(systemId);
			} else {
				impactIds = new HashSet<String>();
			}
			impactIds.add(impactId);
			systemId2ImpactIds.put(systemId, impactIds);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Serialise impact to ecore xml. 
	 * 
	 * @param impact 
	 * @return ecore xml representation of impact.
	 * @throws IOException
	 */
	protected String serializeImpact(Impact impact) throws IOException {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("impact", new EcoreResourceFactoryImpl());

		Notification container = ImpactFactory.eINSTANCE.createNotification();
		container.setTopLevelImpact(impact);
//		Impact current = impact;
//		while (current != null) {
//			container.getImpacts().add(current);
//			current = current.getCause();
//		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		URI uri = URI.createPlatformResourceURI("foo.impact", false); // unmapped,  							
		
		Resource res = set.createResource(uri);
		res.getContents().add(container);
		res.save(outputStream, null);
		
		return outputStream.toString(StandardCharsets.UTF_8);
	}
	
	
	/**
	 * Deserialise impact from ecore xml. 
	 * 
	 * @param impact 
	 * @return ecore xml representation of impact.
	 * @throws IOException
	 */
	protected Impact deserializeImpact(String xml) throws IOException {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("impact", new EcoreResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("saga", new EcoreResourceFactoryImpl());
		InputStream targetStream = new ByteArrayInputStream(xml.getBytes());
				
		Resource resource = set.createResource(URI.createPlatformResourceURI("foo.impact", false));
		resource.load(targetStream, null);
		
		for (EObject eObject : resource.getContents()) {
			if (eObject instanceof Notification) {
				return ((Notification) eObject).getTopLevelImpact();
			}
		}

		throw new IOException("Could not deserialize " + xml);
	}
	
	/**
	 * 
	 * @param systemId
	 * @return
	 */
	public Set<Impact> findBySystemId(String systemId) {
		if (!systemId2ImpactIds.containsKey(systemId)) {
			throw new NoSuchElementException(String.format("No impacts for system with id %s", systemId));
		}
		Set<String> noteIds = systemId2ImpactIds.get(systemId);
		
		Set<Impact> impacts = new HashSet<Impact>();
		
		for (String noteId : noteIds) {
			// TODO add all...
			impacts.add(findById(noteId));
		}
		
		return impacts;	
	}
	
	
	/**
	 * 
	 * @param systemId
	 * @return
	 */
	public Set<String> findXMLBySystemId(String systemId) {
		Set<String> noteIds = systemId2ImpactIds.get(systemId);
		
		Set<String> impacts = new HashSet<>();
		
		for (String noteId : noteIds) {
			impacts.add(findXMLById(noteId));
		}
		return impacts;	
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public String findXMLById(String id) {
		if (repository.existsById(id)) {
			ImpactItem item = repository.findById(id).get();
			return item.getContent();
		}
		throw new NoSuchElementException(String.format("Missing Impact for id %s", id));	
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Impact findById(String id) {
		if (loadedImpacts.containsKey(id)) {
			return loadedImpacts.get(id);
		}
		if (repository.existsById(id)) {
			ImpactItem item = repository.findById(id).get();
			
			try {
				Impact impact = deserializeImpact(item.getContent());
				loadedImpacts.put(item.getSystemId(), impact);
				return impact;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		throw new NoSuchElementException(String.format("Missing Impact for id %s", id));	
	}
}
