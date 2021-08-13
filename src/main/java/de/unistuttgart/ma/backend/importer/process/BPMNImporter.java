package de.unistuttgart.ma.backend.importer.process;

import java.util.Iterator;

import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class BPMNImporter {

	private final URI uri;

	public BPMNImporter(String file) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpmn2", new Bpmn2ResourceFactoryImpl());
		this.uri = URI.createFileURI(file);
	}

	public Process parse() {
		ResourceSet set = new ResourceSetImpl();
		Resource resource = set.getResource(uri, true);

		Process rval = null;
		
		for (EObject eObject : resource.getContents()) {
			Iterator<EObject> it = eObject.eAllContents();
			while (it.hasNext()) {
				EObject eo = it.next();
				if (eo instanceof org.eclipse.bpmn2.Process) {
					rval = (org.eclipse.bpmn2.Process) eo;
				}
				System.err.println(eo.getClass());
			}
		}
		
		return rval;
	}
}
