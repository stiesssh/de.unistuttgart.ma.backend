package de.unistuttgart.ma.backend;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.backend.rest.Alert;
import de.unistuttgart.ma.saga.System;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * 
 * Controller that receives an alert, posted by a SLO monitoring tool.  
 * 
 * @author maumau
 *
 */
@RestController
public class AlertController {
	
	private final NotificationCreationService service;
	private final CreateIssueService issueService;
	private final SystemRepositoryProxy systemRepoProxy; 

	public AlertController(@Autowired NotificationCreationService service, @Autowired SystemRepositoryProxy systemRepoProxy, @Autowired CreateIssueService issueService) {
		this.service = service;
		this.systemRepoProxy = systemRepoProxy;
		this.issueService = issueService;
	}

	/**
	 * 
	 * 
	 * @param alert the alert
	 */
	@PostMapping("/api/alert")
	public void receiveAlert(@RequestBody Alert alert) {
		String sloId = alert.getSloId();
		String archId = alert.getGropiusProjectId();
		
		System system = systemRepoProxy.findByArchitectureId(archId);
		SloRule rule = system.getSloRules().stream().filter(s -> s.getName().equals(sloId)).findFirst().get();
		
		Violation v = ImpactFactory.eINSTANCE.createViolation();
		v.setViolatedRule(rule);
		v.setPeriod(alert.getActualPeriod());
		v.setThreshold(alert.getActualValue());
		
		Set<Notification> notes = service.calculateImpacts(v);
		for (Notification notification : notes) {
			issueService.createIssue(notification);
		}
	}


//	@ExceptionHandler(MissingSystemModelException.class)
//	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//	public ResponseEntity<String> missingSystemModelException(MissingSystemModelException exception) {
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
//	}

}

