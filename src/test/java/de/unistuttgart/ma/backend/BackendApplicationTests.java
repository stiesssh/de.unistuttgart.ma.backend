package de.unistuttgart.ma.backend;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import de.unistuttgart.ma.saga.SagaPackage;

//@SpringBootTest
class BackendApplicationTests {

	@Test
	void playground() {

		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("saga", new EcoreResourceFactoryImpl());
		SagaPackage packageInstance = SagaPackage.eINSTANCE;	
		ResourceSet set = new ResourceSetImpl();
		
		Resource resource = set.createResource(URI.createPlatformResourceURI("foo.saga", false));
		
		
		URI ressourceUri = resource.getURI();
		System.out.println( ressourceUri.toString());
		System.out.println(ressourceUri.segment(ressourceUri.segmentCount() - 1));	
	}
}
