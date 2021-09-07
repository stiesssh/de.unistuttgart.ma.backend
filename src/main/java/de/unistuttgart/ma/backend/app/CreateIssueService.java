package de.unistuttgart.ma.backend.app;

import java.io.IOException;
import java.util.List;

import org.eclipse.bpmn2.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.gropius.api.Component;
import de.unistuttgart.gropius.api.Issue;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQuerier;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQueries;
import de.unistuttgart.ma.backend.utility.ImpactSerializer;
import de.unistuttgart.ma.backend.utility.InterfaceSerializer;
import de.unistuttgart.ma.backend.utility.NotificationSerializer;
import de.unistuttgart.ma.backend.utility.SloRuleSerializer;
import de.unistuttgart.ma.backend.utility.StepSerializer;
import de.unistuttgart.ma.backend.utility.TaskSerializer;
import de.unistuttgart.ma.backend.utility.ViolationSerializer;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;
import de.unistuttgart.ma.saga.SagaStep;

/**
 * Create a Gropius issues for an impact that reached the business process.
 * 
 * @author maumau
 *
 */
@org.springframework.stereotype.Component
public class CreateIssueService {

	// for Serialization
	private final ObjectMapper mapper;
	private final SimpleModule module;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final GropiusApiQuerier querier;

	public CreateIssueService(@Value("${gropius.url}") String uri) {
		assert (uri != null);

		module = new SimpleModule();

		module.addSerializer(Notification.class, new NotificationSerializer(Notification.class));
		module.addSerializer(Impact.class, new ImpactSerializer(Impact.class));
		module.addSerializer(Violation.class, new ViolationSerializer(Violation.class));
		module.addSerializer(SagaStep.class, new StepSerializer(SagaStep.class));
		module.addSerializer(Task.class, new TaskSerializer(Task.class));
		module.addSerializer(ComponentInterface.class, new InterfaceSerializer(ComponentInterface.class));
		module.addSerializer(SloRule.class, new SloRuleSerializer(SloRule.class));

		mapper = new ObjectMapper();
		mapper.registerModule(module);

		querier = new GropiusApiQuerier(uri);
	}

	/**
	 * Create an issue for the given notification at the given issue location.
	 * 
	 * If an open issue that matches the notification (i.e. violation to same SLO
	 * rule, same locations impacted) already exists, no new issues is created.
	 * return the id of the matching and already existing issue instead.
	 * 
	 * @param notification notification to create issue for
	 * @param location     issue location to create new issue at
	 * @return id of the notification's issue
	 * @throws IssueCreationFailedException if the creation of the issue failed
	 */
	public ID createIssue(Notification notification, IssueLocation location) throws IssueCreationFailedException {

		Issue openIssue = getOpenIssueOnLocationForNotification(notification, location);

		if (openIssue != null) {
			logger.info(String.format("Issue \"%s\" alread exist with ID %s", openIssue.getTitle(),
					openIssue.getId().toString()));
			return openIssue.getId();
		}

		String body = createBody(notification);
		String title = createTitle(notification);

		MutationQuery mutation = GropiusApiQueries.getCreateIssueMutation(location.getId(), body, title);

		ID id = querier.queryCreateIssueMutation(mutation).getCreateIssue().getIssue().getId();

		logger.info(String.format("Create Issue. ID : %s, Title : %s", id.toString(), title));

		return id;
	}

	/**
	 * Link one gropius issue to another.
	 * 
	 * The origin issue is the issue that represents the notification and the
	 * destination issue is the one for the violation of the SLO rule.
	 * 
	 * @param origin      source issue of the link
	 * @param destination issue to be linked to
	 * @throws IssueLinkageFailedException If the linking failed
	 */
	public void linkIssue(ID origin, ID destination) throws IssueLinkageFailedException {
		assert (origin != null && destination != null);
		MutationQuery mutation = GropiusApiQueries.getLinkIssueMutation(origin, destination);
		querier.queryLinkIssueMutation(mutation);
	}

	/**
	 * Look at the open issues at the given location. If any of them matches the
	 * given notification, return that issue.
	 * 
	 * @param note     notification to match
	 * @param location location of issues
	 * @return an issue that matches the given notification, or null if no such
	 *         issue exists
	 */
	private Issue getOpenIssueOnLocationForNotification(Notification note, IssueLocation location) {
		assert (note != null && location != null);
		try {
			Query query = querier.queryQuery(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(location.getId())));
			if (!query.getNode().getGraphQlTypeName().equals("Component")) {
				return null;
			}

			List<Issue> bodies = ((Component) query.getNode()).getIssues().getNodes();

			for (Issue issue : bodies) {
				String body = issue.getBody();

				if (isSameIssue(note, body)) {
					return issue;
				}
			}
		} catch (IOException | InterruptedException e) {
			logger.info(String.format("Could not get open issues at location %s because of %s", location.getName(),
					e.getMessage()));
		}
		return null;
	}

	/**
	 * Check whether a Gropius issue body represents a certain notification.
	 * 
	 * It does, if the violated Slo rules and the trace of impacted location is the
	 * same. For now its enough to check the rule and the top most impact.
	 * 
	 * If any thing goes wrong during comparison, they are treated as different.
	 * 
	 * 
	 * @param note the notification
	 * @param body body of a Gropius issue
	 * @return true iff the issues represents the notification, false otherwise
	 */
	public boolean isSameIssue(Notification note, String body) {
		assert (note != null && body != null);

		try {
			JsonNode node = mapper.readTree(getNotificationRepresentation(body));
			if (node.findPath("impactlocation").isMissingNode() || node.findPath("violatedrule").isMissingNode()
					|| node.findPath("impactlocation").findPath("id").isMissingNode()
					|| node.findPath("violatedrule").findPath("id").isMissingNode()) {
				logger.error("wrong json schema, omitting an issue.");
			}

			String locationId = node.findPath("impactlocation").findPath("id").asText();
			String rootcauseId = node.findPath("violatedrule").findPath("id").asText();

			String noteLocationId = note.getTopLevelImpact().getLocationId();
			String noterootCauseId = note.getRootCause().getViolatedRule().getId();

			return locationId.equals(noteLocationId) && rootcauseId.equals(noterootCauseId);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			logger.debug(e.getMessage());
		}
		return false;
	}

	/**
	 * Extract that part of a gropius issue body, that represents the notification.
	 * 
	 * @param body body of a gropius issue
	 * @return string representation of the notification
	 */
	private String getNotificationRepresentation(String body) {
		assert (body != null);

		if (!(body.contains("[//]: # (") && body.contains(")"))) {
			throw new IllegalArgumentException(String.format("Not a valid body : %s", body));
		}

		String[] split1 = body.split("\\(");
		if (split1.length < 2) {
			throw new IllegalArgumentException(String.format("Not a valid body : %s", body));
		}

		String[] split2 = split1[1].split("\\)");
		if (split2.length < 1) {
			throw new IllegalArgumentException(String.format("Not a valid body : %s", body));
		}

		return split2[0];
	}

	/**
	 * Serialise Notification to json.
	 * 
	 * @param note the notification
	 * @return json representation of the notification
	 * @throws JsonProcessingExceptionnote if the serialisation failed.
	 */
	public String parseToJson(Notification note) throws JsonProcessingException {
		return mapper.writeValueAsString(note);
	}

	/**
	 * Create a body for a gropius issue that contains a json represenation of the
	 * given notification.
	 * 
	 * TODO : (re)add some additional human readable information?
	 * 
	 * @param note the notification
	 * @return body for a gropius issue that contains a json representation of the
	 *         notification note
	 */
	public String createBody(Notification note) {
		StringBuilder sb = new StringBuilder();

		// for the machine
		sb.append("[//]: # (");
		try {
			sb.append(parseToJson(note));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		sb.append(")").append("\n");
		// for the human
//		appendHumanLocation(note.getRootCause().getViolatedRule().getGropiusComponent(), sb);
//		sb.append("* Path :\n");
//		Impact current = note.getTopLevelImpact();
//		while (current != null) {
//			appendHumanPathStep(current, sb);
//			current = current.getCause();
//		}

		return sb.toString();
	}

	/**
	 * Create a issue title for a notification.
	 * 
	 * @param note the notification
	 * @return a title for a gropius issue.
	 */
	protected String createTitle(Notification note) {
		assert (note != null);
		StringBuilder sb = new StringBuilder();

		sb.append("Impact on ").append(note.getTopLevelImpact().getLocationContainerType()).append(" ")
				.append(note.getTopLevelImpact().getLocationContainerName()).append(" at ")
				.append(note.getTopLevelImpact().getLocationType()).append(" ")
				.append(note.getTopLevelImpact().getLocationName()).append(" caused by Violation of SLO rule ");
		sb.append(note.getRootCause().getViolatedRule().getName()).append(".");

		return sb.toString();
	}

	protected void appendHumanLocation(Object obj, StringBuilder sb) {
		sb.append("* Location : **").append(obj.toString()).append("**").append("\n");
	}

	protected void appendHhumanRootCause(Violation violation, StringBuilder sb) {
		sb.append("* Root Cause  : Violation of **").append(violation.getViolatedRule().getName()).append("**")
				.append("\n");
	}

	protected void appendHumanPathStep(Impact impact, StringBuilder sb) {
		sb.append("  * **").append(impact.getLocation().toString()).append("**").append("\n");
	}
}
