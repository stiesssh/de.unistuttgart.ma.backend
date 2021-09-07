package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.Project;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.saga.SagaFactory;
import de.unistuttgart.ma.saga.System;
import de.unistuttgart.ma.backend.repository.ImpactItem;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.Violation;

/**
 * Apparently,  the MongoMapper can not handle the complex sturcture of Notifcation and System 
 * java.lang.StackOverflowError
 *	at org.springframework.util.ConcurrentReferenceHashMap.getReference(ConcurrentReferenceHashMap.java:264)
 *	[...]
 * making thing transient would most likely fix this, but i dont want to add additional annotattions to the generated files. 
 *
 * @author maumau
 *
 */
@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
public class RepositoryTest extends TestWithRepo {
	
	@Test
	public void impactRepoProxyTest() throws IOException {
		loadSystem();

		Notification impact = createImpactChain();
		
		Impact impact1 = impact.getTopLevelImpact();
		Impact impact2 = impact1.getCause();
		Impact impact3 = impact2.getCause();
		Impact impact4 = impact3.getCause();

		ImpactItem ii1 = impactRepo.save(new ImpactItem(impact1));
		ImpactItem ii2 = impactRepo.save(new ImpactItem(impact2));
		ImpactItem ii3 = impactRepo.save(new ImpactItem(impact3));
		ImpactItem ii4 = impactRepo.save(new ImpactItem(impact4));
		
		
		// assert
		assertEquals(4, impactRepo.count());
		
		assertTrue(impactRepo.findById(ii1.getId()).isPresent());
		assertTrue(impactRepo.findById(ii2.getId()).isPresent());
		assertTrue(impactRepo.findById(ii3.getId()).isPresent());
		assertTrue(impactRepo.findById(ii4.getId()).isPresent());
		ImpactItem actual1 = impactRepo.findById(ii1.getId()).get();
		ImpactItem actual2 = impactRepo.findById(ii2.getId()).get();
		ImpactItem actual3 = impactRepo.findById(ii3.getId()).get();
		ImpactItem actual4 = impactRepo.findById(ii4.getId()).get();
		
		assertEquals(null, actual4.getCause());
		assertEquals(impact4.getId(), actual3.getCause());
		assertEquals(impact3.getId(), actual2.getCause());
		assertEquals(impact2.getId(), actual1.getCause());
		
	}
	
	@Test
	public void systemRepoProxyTest() throws IOException {	
		loadSystem();
		
		System actual = systemRepoProxy.findById(systemId);
		
		assertNotNull(actual);
		assertNotNull(actual.getArchitecture());
		assertNotNull(actual.getProcesses());
		assertNotNull(actual.getSagas());
		assertNotNull(actual.getSloRules());
		
		assertFalse(actual.getArchitecture().getComponents().isEmpty());
		assertFalse(actual.getProcesses().isEmpty());
		assertFalse(actual.getSagas().isEmpty());
		assertFalse(actual.getSloRules().isEmpty());
		
		assertEquals(system.getId(), actual.getId());
	}
	

	@Test
	public void emptySystemRepoProxyTest() throws IOException {
		// prepare system with resource
		System emptySystem = SagaFactory.eINSTANCE.createSystem();
		String filename = "foo.saga";
		emptySystem.setName(filename);
		
		Project arch = GropiusFactory.eINSTANCE.createProject();
		arch.setId("someId");
		emptySystem.setArchitecture(arch);
		
		Resource resource = set.createResource(URI.createPlatformResourceURI(filename, false));
		resource.getContents().add(emptySystem);		
		
		// execute & assert
		String id = systemRepoProxy.save(emptySystem);
		
		assertEquals(1, systemRepo.count());
		
		System actual = systemRepoProxy.findById(id);
		
		assertNotNull(actual);
		assertEquals(emptySystem.getId(), actual.getId());
	}
}