package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.importer.SagaImporterService;
import de.unistuttgart.ma.backend.repository.ImpactRepository;
import de.unistuttgart.ma.backend.repository.ImpactRepositoryProxy;
import de.unistuttgart.ma.backend.repository.SystemRepository;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.saga.impact.Impact;
import de.unistuttgart.ma.saga.impact.ImpactFactory;
import de.unistuttgart.ma.saga.impact.Violation;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
public abstract class TestWithRepo {
	
	NotificationCreationService computationService;
	NotificationRetrievalService retrievalService;
	SagaImporterService importService;
	Controller controller;
	
	SystemRepositoryProxy systemRepoProxy;
	@Autowired SystemRepository systemRepo;
	
	ImpactRepositoryProxy notificationRepoProxy;
	@Autowired ImpactRepository notificationRepo;
	

	de.unistuttgart.ma.saga.System system; 
	String systemId = "60fa9cadc736ff6357a89a9b";
	
	ResourceSet set;

	@BeforeEach
	public void setUp() {
		set = new ResourceSetImpl();
		
		systemRepoProxy = new SystemRepositoryProxy(systemRepo, set);
		notificationRepoProxy = new ImpactRepositoryProxy(notificationRepo, set);
		
		importService = new SagaImporterService(systemRepoProxy, set);
		retrievalService = new NotificationRetrievalService(notificationRepoProxy, set);
		computationService = new NotificationCreationService(notificationRepoProxy, systemRepoProxy);
		
		systemRepo.deleteAll();
		notificationRepo.deleteAll();
	}
	
	
	/**
	 * load the t2 store
	 * 
	 * @throws IOException
	 */
	public void loadSystem() throws IOException  {
		long size = systemRepo.count();
		String xml = Files.readString(Paths.get("src/test/resources/", "t2_base_saga.saga"), StandardCharsets.UTF_8);					
		importService.parse(xml);
		
		system = systemRepoProxy.findById(systemId);
				
		assertEquals(size + 1, systemRepo.count());
	}
	
	public void loadImpact() {
		long size = notificationRepo.count();
		Impact impact = createImpactChain();
		notificationRepoProxy.save(impact, systemId);
		assertEquals(size + 1, notificationRepo.count());
	}
	
	
	public Impact createImpactChain() {
		// create 
		ComponentInterface creditInstituteFace = system.getComponentInterfaceById("5e8cf780c585a029");
		ComponentInterface paymentFace = system.getComponentInterfaceById("5e8cf760d345a028");
		SloRule rule = system.getSloForNode(creditInstituteFace).iterator().next();

		Violation violation = ImpactFactory.eINSTANCE.createViolation();
		violation.setViolatedRule(rule);

		Impact impact = ImpactFactory.eINSTANCE.createImpact();
		impact.setLocation(paymentFace);
		impact.setCause(violation);
		
		return impact;
	}

}
