package de.unistuttgart.ma.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.backend.rest.Alert;
import de.unistuttgart.ma.saga.System;
import de.unistuttgart.ma.saga.impact.ImpactFactory;
import de.unistuttgart.ma.saga.impact.Violation;

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
	private final SystemRepositoryProxy systemRepoProxy; 

	public AlertController(@Autowired NotificationCreationService service, @Autowired SystemRepositoryProxy systemRepoProxy) {
		this.service = service;
		this.systemRepoProxy = systemRepoProxy;
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
		// TODO : set value @ violation according to alert
		service.calculateImpacts(v);
	}


//	@ExceptionHandler(MissingSystemModelException.class)
//	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//	public ResponseEntity<String> missingSystemModelException(MissingSystemModelException exception) {
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
//	}

}

