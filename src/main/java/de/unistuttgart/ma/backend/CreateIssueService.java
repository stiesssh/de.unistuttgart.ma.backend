package de.unistuttgart.ma.backend;

import java.io.IOException;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Task;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.gropius.IssueLocation;
import de.unistuttgart.gropius.api.ComponentInterface;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exporter.impact.ImpactListSerializer;
import de.unistuttgart.ma.backend.exporter.impact.InterfaceSerializer;
import de.unistuttgart.ma.backend.exporter.impact.NotificationListSerializer;
import de.unistuttgart.ma.backend.exporter.impact.NotificationTreeSerializer;
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

	// TODO @Value for uri 
	public CreateIssueService(String uri) {
		module = new SimpleModule();
		
		module.addSerializer(Notification.class, new NotificationListSerializer(Notification.class));
		module.addSerializer(Impact.class, new ImpactListSerializer(Impact.class));
		module.addSerializer(Violation.class, new ViolationSerializer(Violation.class));
		module.addSerializer(SagaStep.class, new StepSerializer(SagaStep.class));
		module.addSerializer(Task.class, new TaskSerializer(Task.class));
		module.addSerializer(ComponentInterface.class, new InterfaceSerializer(ComponentInterface.class));
		module.addSerializer(SloRule.class, new SloRuleSerializer(SloRule.class));
		//module.addSerializer(Notification.class, new NotificationTreeSerializer(Notification.class));
		//module.addSerializer(Impact.class, new ImpactTreeSerializer(Impact.class));

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

		String body = "";
		try {
			body = parseToJson(topLevelImpact);
		} catch (JsonProcessingException e) {
			logger.info(String.format("Could not serialize to JOSN"));
		}
		String title = createTitle(topLevelImpact);

		MutationQuery mutation = GropiusApiQueries.getCreateIssueMutation("", body, title);

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
}
