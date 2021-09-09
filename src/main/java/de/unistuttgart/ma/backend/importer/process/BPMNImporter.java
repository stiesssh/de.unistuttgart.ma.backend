package de.unistuttgart.ma.backend.importer.process;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
/**
 * A {@code BPMNImporter} imports a business process.
 */
public class BPMNImporter {

	
	private final Resource resource; 
	private final String bpmn;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Create a new importer that parses the given process. 
	 * @param bpmn
	 */
	public BPMNImporter(String bpmn) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpmn2", new Bpmn2ResourceFactoryImpl());
		ResourceSet set = new ResourceSetImpl();
		this.resource = set.createResource(URI.createPlatformResourceURI("foo.bpmn2", false));
		this.bpmn = bpmn;
	}

	/**
	 * Get the process.
	 * 
	 * @return the process 
	 * @throws ModelCreationFailedException if parsing the business process failed. 
	 */
	public Process parse() throws ModelCreationFailedException {
		InputStream targetStream = new ByteArrayInputStream(bpmn.getBytes());
		try {
			resource.load(targetStream, null);
		} catch (IOException e) {
			throw new ModelCreationFailedException("Could not parse Process : " + e.getMessage(), e);
		}

		Process rval = null;
		
		for (EObject eObject : resource.getContents()) {
			Iterator<EObject> it = eObject.eAllContents();
			while (it.hasNext()) {
				EObject eo = it.next();
				if (eo instanceof org.eclipse.bpmn2.Process) {
					rval = (org.eclipse.bpmn2.Process) eo;
				}
			}
		}
		
		if (rval != null) {
			logger.info(String.format("successfully parsed Process %s with %d flowelements." , rval.getName(), rval.getFlowElements().size()));
			return rval;
		}
		throw new ModelCreationFailedException("No process to found", null);
	}
}
