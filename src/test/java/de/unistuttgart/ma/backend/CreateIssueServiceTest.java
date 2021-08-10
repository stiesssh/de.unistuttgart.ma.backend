package de.unistuttgart.ma.backend;

import java.io.IOException;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.Issue;
import de.unistuttgart.gropius.slo.SloFactory;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.saga.SagaFactory;
import de.unistuttgart.ma.saga.SagaStep;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * 
 * @author maumau
 *
 */
public class CreateIssueServiceTest extends TestWithMockServer {

	String uri; 
	CreateIssueService service;

	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		uri = "http://localhost:" + port + gropius;
		service = new CreateIssueService(uri);
	}
	
	/**
	 * 
	 * 
	 * TODO assert request content ?? 
	 *  
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testCreateIssueMutation() throws IOException, InterruptedException {				
		
		Notification note = createImpact();
		service.createIssue(note);
		
		verifyGropiusIssue();
	}
	
	@Test
	public void testCreateJson() throws JsonProcessingException {

		Notification note = createImpact();
		
		CreateIssueService service = new CreateIssueService(uri);
		System.out.println(service.parseToJson(note));
		
		// TODO : assert !?!?

	}
	
	private Notification createImpact() {
		Task task = Bpmn2Factory.eINSTANCE.createTask();
		task.setId("taskid");
		task.setName("taskname");
		SagaStep step = SagaFactory.eINSTANCE.createSagaStep();
		step.setId("stepid");
		step.setName("stepname");
		SloRule rule = SloFactory.eINSTANCE.createSloRule();
		rule.setId("sloruleid");
		rule.setName("sloruleName");
		Issue issue = GropiusFactory.eINSTANCE.createIssue();
		issue.setId("issueId");
		
		Impact impact = ImpactFactory.eINSTANCE.createImpact();
		impact.setLocation(task);
		impact.setId("impact1");
		Impact impact2 = ImpactFactory.eINSTANCE.createImpact();
		impact2.setLocation(step);
		impact2.setId("impact2");
		
		impact.setCause(impact2);

		Violation violation = ImpactFactory.eINSTANCE.createViolation();
		violation.setViolatedRule(rule);
		violation.setIssue(issue);
		violation.setPeriod(0.0);
		violation.setThreshold(0.0);
		
		Notification note = ImpactFactory.eINSTANCE.createNotification();
		note.setTopLevelImpact(impact);
		note.setRootCause(violation);
		
		return note;
	}
}
