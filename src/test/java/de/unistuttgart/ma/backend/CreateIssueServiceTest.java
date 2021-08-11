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
public class CreateIssueServiceTest extends TestWithRepoAndMockServers {

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
		loadSystem();
		Notification note = createImpactChain();
		service.createIssue(note);
		
		verifyGropiusIssue(1);
	}
	
	@Test
	public void testCreateJson() throws IOException {
		loadSystem();
		Notification note = createImpactChain();
		
		CreateIssueService service = new CreateIssueService(uri);
		System.out.println(service.parseToJson(note));
		
		// TODO : assert !?!?

	}
	
	@Test
	public void testHumanBody() throws IOException {
		loadSystem();
		Notification note = createImpactChain();
		
		CreateIssueService service = new CreateIssueService(uri);
		System.out.println(service.createHumanBody(note));
		
		// TODO : assert !?!?

	}
}
