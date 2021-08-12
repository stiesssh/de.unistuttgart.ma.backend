package de.unistuttgart.ma.backend;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.impact.Notification;

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
	 * @throws IssueCreationFailedException 
	 */
	@Test
	public void testCreateIssueMutation() throws IssueCreationFailedException, IOException {				
		loadSystem();
		Notification note = createImpactChain();
		IssueLocation location = GropiusFactory.eINSTANCE.createComponent();
		location.setId("5ece9ed4662c5013");
		service.createIssue(note, location);
		
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
