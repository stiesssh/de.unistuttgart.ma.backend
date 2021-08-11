package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Task;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.Issue;
import de.unistuttgart.gropius.slo.SloFactory;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.importer.SagaImporterService;
import de.unistuttgart.ma.backend.repository.ImpactRepository;
import de.unistuttgart.ma.backend.repository.ImpactRepositoryProxy;
import de.unistuttgart.ma.backend.repository.SystemRepository;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;
import de.unistuttgart.ma.saga.SagaFactory;
import de.unistuttgart.ma.saga.SagaStep;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
public abstract class TestWithRepo {
	
	protected NotificationCreationService computationService;
	protected NotificationRetrievalService retrievalService;
	protected SagaImporterService importService;
	protected Controller controller;
	
	protected SystemRepositoryProxy systemRepoProxy;
	@Autowired SystemRepository systemRepo;
	
	protected ImpactRepositoryProxy notificationRepoProxy;
	@Autowired ImpactRepository notificationRepo;
	

	protected de.unistuttgart.ma.saga.System system; 
	protected String systemId = "60fa9cadc736ff6357a89a9b";
	protected String gropiusId = "5e8cc17ed645a00c";
	
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
//		long size = notificationRepo.count();
//		Notification note = createImpactChain();
//		notificationRepoProxy.save(note, systemId);
//		assertEquals(size + 1, notificationRepo.count());
	}
	
	
	public Notification createImpactChain() {
		// create 
		ComponentInterface creditInstituteFace = system.getComponentInterfaceById("5e8cf780c585a029");
		ComponentInterface paymentFace = system.getComponentInterfaceById("5e8cf760d345a028");
		SloRule rule = system.getSloForNode(creditInstituteFace).iterator().next();
	
		
		Issue issue = GropiusFactory.eINSTANCE.createIssue();
		issue.setId("slo-vioaltion-issue");
		
		Impact impact1 = ImpactFactory.eINSTANCE.createImpact();
		impact1.setLocation(creditInstituteFace);
		impact1.setId("impact-ci");
		Impact impact2 = ImpactFactory.eINSTANCE.createImpact();
		impact2.setLocation(paymentFace);
		impact2.setId("impact-pay");
		
		impact2.setCause(impact1);

		Violation violation = ImpactFactory.eINSTANCE.createViolation();
		violation.setViolatedRule(rule);
		violation.setIssue(issue);
		violation.setPeriod(0.0);
		violation.setThreshold(0.0);
		
		Notification note = ImpactFactory.eINSTANCE.createNotification();
		note.setTopLevelImpact(impact2);
		note.setRootCause(violation);
		
		return note;
	}

}
