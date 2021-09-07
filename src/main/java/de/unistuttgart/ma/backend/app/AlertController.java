package de.unistuttgart.ma.backend.app;

import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopify.graphql.support.ID;

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
 * Controller with an end point to receive alerts from a monitoring tool.
 * 
 * Upon receiving an alert it calculates the impacts of the reported violation,
 * creates an issue for each impact that reaches the business process and links
 * each created issue to the issue that the SLA manager (solomon) supposedly
 * created for the initial violation.
 * 
 * TODO : currently, gropius can only attach issues to components of interface.
 * however the hereby calculated impact is on the business process. until there
 * is an option to attach an issue to the entire project of gropius, all issues
 * will be attached to the root cause component, even though that is not
 * practicable, as it implies that the user already knows the root cause to look
 * up the issue.
 * 
 * @author maumau
 *
 */
@RestController
public class AlertController {

	private final CalculateNotificationService notificationService;
	private final CreateIssueService issueService;
	private final SystemRepositoryProxy systemRepoProxy;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public AlertController(@Autowired CalculateNotificationService notificationService,
			@Autowired SystemRepositoryProxy systemRepoProxy, @Autowired CreateIssueService issueService) {
		assert (notificationService != null && systemRepoProxy != null && issueService != null);
		this.notificationService = notificationService;
		this.systemRepoProxy = systemRepoProxy;
		this.issueService = issueService;
	}

	/**
	 * Receive alerts, get the violation, calculate its impacts and create issues.
	 * 
	 * Delegates the latter two taks to the respective services.
	 * 
	 * @param alert the alert
	 * @throws IssueCreationFailedException if an issue could not be created
	 * @throws IssueLinkageFailedException  if an issue could not be linked
	 */
	@PostMapping("/api/alert")
	public void receiveAlert(@RequestBody Alert alert)
			throws IssueCreationFailedException, IssueLinkageFailedException {
		if (alert == null) {
			throw new IllegalArgumentException("Alert is null");
		}
		logger.info(String.format("received alert for SLO rule %s", alert.getSloName()));

		String sloId = alert.getSloId();
		String archId = alert.getGropiusProjectId();
		ID relatedIssueId = new ID(alert.getIssueId());

		System system = systemRepoProxy.findByArchitectureId(archId);
		SloRule rule = system.getSloById(sloId);

		Violation v = ImpactFactory.eINSTANCE.createViolation();
		v.setViolatedRule(rule);
		v.setPeriod(alert.getActualPeriod());
		v.setThreshold(alert.getActualValue());
		v.setStartTime(alert.getAlertTime());

		Set<Notification> notes = notificationService.calculateImpacts(v);
		logger.info(String.format("calculated %d impacts", notes.size()));

		for (Notification notification : notes) {
			// TODO : if ever possible do attach issue to more suitable location
			IssueLocation location = rule.getGropiusComponent();

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
