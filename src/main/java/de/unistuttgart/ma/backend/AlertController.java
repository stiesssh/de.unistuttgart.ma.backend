package de.unistuttgart.ma.backend;

import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;
import de.unistuttgart.ma.backend.repository.SystemRepositoryProxy;
import de.unistuttgart.ma.backend.rest.Alert;
import de.unistuttgart.ma.saga.System;
import de.unistuttgart.ma.impact.ImpactFactory;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * 
 * Controller that receives an alert, form an SLO monitoring tool.
 * 
 * Upon receiving an alert it calculates the impacts of the reported violation,
 * creates an issue for each top level impact and links each created issue to
 * the issue that the slo manager (solomon) supposedly created for the initial
 * slo violation.
 * 
 * TODO : the location of the created issues is faked, because belong to they
 * dont belong to any component or interface of the architecture but to the
 * overlying process. and there is no process in gropius.
 * TODO : ask sandro, where to place the issues. 
 * 
 * @author maumau
 *
 */
@RestController
public class AlertController {

	private final NotificationCreationService service;
	private final CreateIssueService issueService;
	private final SystemRepositoryProxy systemRepoProxy;
	
	// TODO unfake
	private final String fakeIssueLocation;

	public AlertController(@Autowired NotificationCreationService service,
			@Autowired SystemRepositoryProxy systemRepoProxy, @Autowired CreateIssueService issueService, @Value("${fakeIssueLocation}") String fakeIssueLocation) {
		this.service = service;
		this.systemRepoProxy = systemRepoProxy;
		this.issueService = issueService;
		this.fakeIssueLocation = fakeIssueLocation;
	}

	/**
	 * 
	 * 
	 * @param alert the alert
	 * @throws IssueCreationFailedException
	 * @throws IssueLinkageFailedException
	 */
	@PostMapping("/api/alert")
	public void receiveAlert(@RequestBody Alert alert)
			throws IssueCreationFailedException, IssueLinkageFailedException {
		String sloId = alert.getSloId();
		String archId = alert.getGropiusProjectId();
		ID relatedIssueId = new ID(alert.getIssueId());

		System system = systemRepoProxy.findByArchitectureId(archId);
		SloRule rule = system.getSloRules().stream().filter(s -> s.getName().equals(sloId)).findFirst().get();

		Violation v = ImpactFactory.eINSTANCE.createViolation();
		v.setViolatedRule(rule);
		v.setPeriod(alert.getActualPeriod());
		v.setThreshold(alert.getActualValue());

		Set<Notification> notes = service.calculateImpacts(v);

		for (Notification notification : notes) {
			// IssueLocation location = notification.getTopLevelImpact().getLocation(); //
			// in theory.
			// TODO : unfake!!
			IssueLocation location = GropiusFactory.eINSTANCE.createComponent();
			location.setId(fakeIssueLocation);
			ID issueId = issueService.createIssue(notification, location);
			issueService.linkIssue(issueId, relatedIssueId);
		}
	}

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> noSuchElementException(NoSuchElementException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}

	@ExceptionHandler(IssueCreationFailedException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> issueCreationFailedException(IssueCreationFailedException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}

	@ExceptionHandler(IssueLinkageFailedException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> issueLinkageFailedException(IssueLinkageFailedException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}
}
