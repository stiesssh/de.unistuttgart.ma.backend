package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Task;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
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
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.app.ModelController;
import de.unistuttgart.ma.backend.app.CalculateNotificationService;
import de.unistuttgart.ma.backend.app.ModelService;
import de.unistuttgart.ma.backend.repository.ImpactRepository;
import de.unistuttgart.ma.backend.repository.SystemRepository;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;
import de.unistuttgart.ma.saga.SagaStep;
import de.unistuttgart.ma.saga.System;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
public abstract class TestWithRepo {
	
	protected CalculateNotificationService computationService;
	protected ModelService importService;
	protected ModelController controller;
	
	protected SystemRepositoryProxy systemRepoProxy;
	@Autowired protected SystemRepository systemRepo;
	
	@Autowired protected ImpactRepository impactRepo;
	

	protected de.unistuttgart.ma.saga.System system; 
	protected String systemId = "60fa9cadc736ff6357a89a9b";
	protected String gropiusId = "5e8cc17ed645a00c";
	
	ResourceSet set;

	@BeforeEach
	public void setUp() {
		set = new ResourceSetImpl();
		
		systemRepoProxy = new SystemRepositoryProxy(systemRepo, set);
		
		importService = new ModelService(systemRepoProxy, set);
		computationService = new CalculateNotificationService(systemRepoProxy, impactRepo);
		
		systemRepo.deleteAll();
		impactRepo.deleteAll();
	}
	
	
	/**
	 * load the t2 store
	 * 
	 * @throws IOException
	 */
	public void loadSystem() throws IOException  {
		long size = systemRepo.count();
		String xml = Files.readString(Paths.get("src/test/resources/", "t2_base_saga.saga"), StandardCharsets.UTF_8);					
		
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		
		// create new resource, other wise we wont load, but instead just reuse stuff from the previous parsing. 
		Resource recource = set.createResource(URI.createPlatformResourceURI("foo.saga", false));
		recource.load(inputStream, null);

		for (EObject eObject : recource.getContents()) {
			if (eObject instanceof System) {
				systemRepoProxy.save((System) eObject);
			}
		}

		system = systemRepoProxy.findById(systemId);
				
		assertEquals(size + 1, systemRepo.count());
	}
	
	public Notification createImpactChain() {
		// create 
		ComponentInterface creditInstituteFace = system.getComponentInterfaceById("5e8cf780c585a029");
		ComponentInterface paymentFace = system.getComponentInterfaceById("5e8cf760d345a028");
		SagaStep step = system.getSagaStepById("paymentStep");
		FlowElement task = system.getTaskById("Task_4");
		SloRule rule = system.getSloById("CI_respT_slo");
	
		
		Issue issue = GropiusFactory.eINSTANCE.createIssue();
		issue.setId("slo-vioaltion-issue");
		
		Impact impact1 = ImpactFactory.eINSTANCE.createImpact();
		impact1.setLocation(creditInstituteFace);
		impact1.setId("impact-ci");
		Impact impact2 = ImpactFactory.eINSTANCE.createImpact();
		impact2.setLocation(paymentFace);
		impact2.setId("impact-pay");

		Impact impact22 = ImpactFactory.eINSTANCE.createImpact();
		impact22.setLocation(step);
		impact22.setId("impact-step");
		
		Impact impact3 = ImpactFactory.eINSTANCE.createImpact();
		impact3.setLocation(task);
		impact3.setId("impact-task");
		
		impact2.setCause(impact1);
		impact22.setCause(impact2);
		impact3.setCause(impact22);

		Violation violation = ImpactFactory.eINSTANCE.createViolation();
		violation.setViolatedRule(rule);
		violation.setIssue(issue);
		violation.setPeriod(0.0);
		violation.setThreshold(0.0);
		violation.setStartTime(LocalDateTime.now());
		
		Notification note = ImpactFactory.eINSTANCE.createNotification();
		note.setTopLevelImpact(impact3);
		note.setRootCause(violation);
		
		return note;
	}

}
