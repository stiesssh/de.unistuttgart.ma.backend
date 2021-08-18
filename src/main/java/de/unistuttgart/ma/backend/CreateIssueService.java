package de.unistuttgart.ma.backend;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Task;
import org.springframework.beans.factory.annotation.Value;
import de.unistuttgart.gropius.api.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.shopify.graphql.support.ID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.gropius.api.Issue;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exceptions.IssueCreationFailedException;
import de.unistuttgart.ma.backend.exceptions.IssueLinkageFailedException;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQuerier;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQueries;
import de.unistuttgart.ma.backend.serializer.ImpactSerializer;
import de.unistuttgart.ma.backend.serializer.InterfaceSerializer;
import de.unistuttgart.ma.backend.serializer.NotificationSerializer;
import de.unistuttgart.ma.backend.serializer.SloRuleSerializer;
import de.unistuttgart.ma.backend.serializer.StepSerializer;
import de.unistuttgart.ma.backend.serializer.TaskSerializer;
import de.unistuttgart.ma.backend.serializer.ViolationSerializer;
import de.unistuttgart.ma.saga.IdentifiableElement;
import de.unistuttgart.ma.saga.SagaStep;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * creates gropius issues for impacts.
 * 
 * triggered by new top level impact.
 * 
 * @author maumau
 *
 */
@org.springframework.stereotype.Component
public class CreateIssueService {

	private final ObjectMapper mapper;
	private final SimpleModule module;

	private final GropiusApiQuerier querier;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public CreateIssueService(@Value("${gropius.url}") String uri) {
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
	 * 
	 * @param notification notification to create issue for
	 * @param location     issue location to create new issue at
	 * @return id of created issue
	 * @throws IssueCreationFailedException
	 */
	public ID createIssue(Notification notification, IssueLocation location) throws IssueCreationFailedException {

		Issue openIssue = getOpenIssueOnLocationForNotification(notification, location);

		if (openIssue != null) {
			logger.info(String.format("Issue \"%s\" alread exist with ID %s", openIssue.getTitle(), openIssue.getId().toString()));
			return openIssue.getId();
		}

			String body = createBody(notification);
			String title = createTitle(notification);

			MutationQuery mutation = GropiusApiQueries.getCreateIssueMutation(location.getId(), body, title);

			ID id = querier.queryCreateIssueMutation(mutation).getCreateIssue().getIssue().getId();

			logger.info(String.format("Create Issue with ID %s", id.toString()));

			return id;
	}

	private Issue getOpenIssueOnLocationForNotification(Notification note, IssueLocation location) {
		try {
			Query query = querier.queryQuery(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID(location.getId())));
			if (!query.getNode().getGraphQlTypeName().equals("Component")) {
				return null;
			}
			
			List<Issue> bodies = ((Component) query.getNode()).getIssues().getNodes();
			
			for (Issue issue : bodies) {
				String body = issue.getBody();
				body = body.split("\\(")[1];
				body = body.split("\\)")[0];

				if (isSameIssue(note, body)) {
					return issue;
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * if any things goes wrong during comparison, treat them as different.
	 * 
	 * 
	 * @param note
	 * @param json
	 * @return
	 */
	public boolean isSameIssue(Notification note, String json) {
		try {
			JsonNode node = mapper.readTree(json);
			String locationId = node.findPath("location").findPath("id").asText();
			String rootcauseId = node.findPath("rootcause").findPath("id").asText();

			String noteLocationId = note.getTopLevelImpact().getLocationId();
			String noterootCauseId = note.getRootCause().getViolatedRule().getId();

			return locationId.equals(noteLocationId) && rootcauseId.equals(noterootCauseId);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Parse impact chain to json.
	 * 
	 * @param topLevelImpact head of the chain
	 * @return json representation of the impact chain
	 * @throws JsonProcessingException
	 */
	protected String parseToJson(Notification topLevelImpact) throws JsonProcessingException {
		return mapper.writeValueAsString(topLevelImpact);
	}

	/**
	 * 
	 * @param json representation of impact chain
	 * @return body for issue
	 */
	protected String createBody(Notification note) {
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
		appendHumanLocation(note.getRootCause().getViolatedRule().getGropiusComponent(), sb);
		sb.append("* Path :\n");
		Impact current = note.getTopLevelImpact();
		while (current != null) {
			appendHumanPathStep(current, sb);
			current = current.getCause();
		}

		return sb.toString();
	}

	/**
	 * 
	 * Create Issue title for given impact.
	 * 
	 * Title concists of... TODO
	 * 
	 * @param topLevelImpact the impact to create a title for.
	 * @return the title of the issue to be.
	 */
	protected String createTitle(Notification topLevelImpact) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ").append(Instant.now().toString()).append(" ]");
		sb.append("Impact on ").append(getLocationName(topLevelImpact.getTopLevelImpact()))
				.append(" caused by Violation of ");
		sb.append(topLevelImpact.getRootCause().getViolatedRule().getName());
		return sb.toString();
	}

	/**
	 * 
	 * @param topLevelImpact
	 * @return
	 */
	protected String getLocationName(Impact topLevelImpact) {
		if (topLevelImpact.getLocation() instanceof IssueLocation) {
			return ((IssueLocation) topLevelImpact.getLocation()).getName();
		}
		if (topLevelImpact.getLocation() instanceof IdentifiableElement) {
			return ((IdentifiableElement) topLevelImpact.getLocation()).getName();
		}
		if (topLevelImpact.getLocation() instanceof FlowElement) {
			return ((FlowElement) topLevelImpact.getLocation()).getName();
		}
		throw new IllegalStateException("illegal model");
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

	/**
	 * 
	 * @param origin
	 * @param destination
	 * @throws IssueLinkageFailedException
	 */
	public void linkIssue(ID origin, ID destination) throws IssueLinkageFailedException {
		MutationQuery mutation = GropiusApiQueries.getLinkIssueMutation(origin, destination);
		querier.queryLinkIssueMutation(mutation);
	}
}
