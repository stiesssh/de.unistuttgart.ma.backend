package de.unistuttgart.ma.backend.controller;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.unistuttgart.ma.backend.AlertController;
import de.unistuttgart.ma.backend.CreateIssueService;
import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.rest.Alert;

public class AlertControllerTest extends TestWithRepoAndMockServers{
	
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
		
		alert = new Alert("5e8cc17ed645a00c", 1.0, 2.0, "CI_avail_slo", "CI_avail_slo");
	}
	
	@Test 
	public void test() throws IOException{
		loadSystem();
		controller.receiveAlert(alert);
		verifyGropiusIssue(2);
	}
	

}