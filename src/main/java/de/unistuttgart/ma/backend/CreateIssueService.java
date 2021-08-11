package de.unistuttgart.ma.backend;

import java.io.IOException;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exporter.impact.ImpactSerializer;
import de.unistuttgart.ma.backend.exporter.impact.InterfaceSerializer;
import de.unistuttgart.ma.backend.exporter.impact.NotificationSerializer;
import de.unistuttgart.ma.backend.exporter.impact.SloRuleSerializer;
import de.unistuttgart.ma.backend.exporter.impact.StepSerializer;
import de.unistuttgart.ma.backend.exporter.impact.TaskSerializer;
import de.unistuttgart.ma.backend.exporter.impact.ViolationSerializer;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQuerier;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQueries;
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
@Component
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
	 * Create a Gropius issue for given impact.
	 * 
	 * @param topLevelImpact impact to create issue for
	 */
	public void createIssue(Notification topLevelImpact) {

		String body = createHumanBody(topLevelImpact);
		String title = createTitle(topLevelImpact);
		
		// TODO unfake the issue location
		MutationQuery mutation = GropiusApiQueries.getCreateIssueMutation("5ecd41d2e205a003", body, title);

		try {
			querier.queryMutation(mutation);
		} catch (IOException | InterruptedException e) {
			logger.info(
					String.format("Issue creation for Impact %s failed. %s", topLevelImpact.getId(), e.getMessage()));
		}
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
	protected String createHumanBody(Notification note) {
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
		appendHhumanRootCause(note.getRootCause(), sb);
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
		sb.append("Impact on ").append(getLocationName(topLevelImpact.getTopLevelImpact())).append(" caused by Violation of ");
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
		sb.append("* Root Cause  : Violation of **").append(violation.getViolatedRule().getName()).append("**").append("\n");
	}
	protected void appendHumanPathStep(Impact impact, StringBuilder sb) {
		sb.append("  * **").append(impact.getLocation().toString()).append("**").append("\n");
	}
}
