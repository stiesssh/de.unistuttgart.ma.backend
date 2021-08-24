package de.unistuttgart.ma.backend.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.app.AlertController;
import de.unistuttgart.ma.backend.app.CreateIssueService;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;
import de.unistuttgart.ma.backend.rest.Alert;

public class AlertControllerTest extends TestWithRepoAndMockServers {

	String uri;
	CreateIssueService service;

	AlertController controller;

	Alert alert;

	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		uri = "http://localhost:" + port + gropius;
		service = new CreateIssueService(uri);
		controller = new AlertController(computationService, systemRepoProxy, service);

		alert = new Alert(0.0, 0.0, "CI_respT_slo", "CI_respT_slo", LocalDateTime.now(), "CI_respT_slo", "CI_respT_slo",
				"trigger", gropiusId, issueLocationId, "todo_linkedissueid");

	}

	@Test
	public void test() throws IOException, IssueCreationFailedException, IssueLinkageFailedException {
		loadSystem();
		controller.receiveAlert(alert);
		verifyPostIssueGropius(3); // 1 creations, 2 linkages
		verifyGetIssueGropius(2);
	}
}
