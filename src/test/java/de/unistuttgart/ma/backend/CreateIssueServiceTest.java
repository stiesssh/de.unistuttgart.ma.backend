package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
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
		String actual = service.parseToJson(note);
	
		System.out.println(actual);
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();

		InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./json/notification.schema.json");
		

		JsonNode json = objectMapper.readTree(actual);
		JsonSchema schema = schemaFactory.getSchema(schemaStream);
		Set<ValidationMessage> validationResult = schema.validate(json);

		// print validation errors
		if (!validationResult.isEmpty()) {
			validationResult.forEach(vm -> System.out.println(vm.getMessage()));
			fail();
		}
		
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
