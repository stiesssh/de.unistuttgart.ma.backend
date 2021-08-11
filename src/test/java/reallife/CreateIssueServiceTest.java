package reallife;

import java.io.IOException;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.Issue;
import de.unistuttgart.gropius.slo.SloFactory;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.saga.SagaFactory;
import de.unistuttgart.ma.saga.SagaStep;
import de.unistuttgart.ma.backend.CreateIssueService;
import de.unistuttgart.ma.backend.TestWithRepo;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * 
 * @author maumau
 *
 */
public class CreateIssueServiceTest extends TestWithRepo {

	String uri = "http://localhost:8080/api"; 
	CreateIssueService service;

	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
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
	 * @throws IssueLinkageFailedException 
	 */
	@Test
	public void testCreateIssueMutation() throws IOException, InterruptedException, IssueCreationFailedException, IssueLinkageFailedException {				
		loadSystem();
		Notification note = createImpactChain();
		ID id = service.createIssue(note);
		service.linkIssue(id, new ID("5ecd5d74e135b005"));
	}
}
