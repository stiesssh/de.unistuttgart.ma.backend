package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.ma.backend.app.CreateIssueService;
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
		mockNoIssueGropius();
		Notification note = createImpactChain();
		IssueLocation location = GropiusFactory.eINSTANCE.createComponent();
		location.setId(issueLocationId);
		ID actual = service.createIssue(note, location);
		
		assertEquals("5ecbf9b233d6502f", actual.toString());
		
		verifyPostIssueGropius(1);
		verifyGetIssueGropius(1);
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
	public void testOpenCreateIssueMutation() throws IssueCreationFailedException, IOException {				
		loadSystem();
		mockOpenIssueGropius();
		Notification note = createImpactChain();
		IssueLocation location = GropiusFactory.eINSTANCE.createComponent();
		location.setId(issueLocationId);
		ID actual = service.createIssue(note, location);
		
		assertEquals("5ed60349e7385001", actual.toString());
		
		verifyPostIssueGropius(0);
		verifyGetIssueGropius(1);
	}
	
	@Test
	public void testCreateJson() throws IOException {
		loadSystem();
		Notification note = createImpactChain();
		
		CreateIssueService service = new CreateIssueService(uri);
		String json = service.parseToJson(note);
	
		System.out.println(json);
		
		// TODO : assert !?!?

	}
	
	@Test
	public void testHumanBody() throws IOException {
		loadSystem();
		Notification note = createImpactChain();
		
		CreateIssueService service = new CreateIssueService(uri);
		System.out.println(service.createBody(note));
		
		// TODO : assert !?!?

	}
}
