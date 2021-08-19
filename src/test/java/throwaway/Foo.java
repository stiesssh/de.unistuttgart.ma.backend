package throwaway;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.api.CloseIssueInput;
import de.unistuttgart.gropius.api.CloseIssuePayloadQuery;
import de.unistuttgart.gropius.api.CloseIssuePayloadQueryDefinition;
import de.unistuttgart.gropius.api.Component;
import de.unistuttgart.gropius.api.ComponentQuery;
import de.unistuttgart.gropius.api.ComponentQuery.IssuesArguments;
import de.unistuttgart.gropius.api.ComponentQuery.IssuesArgumentsDefinition;
import de.unistuttgart.gropius.api.ComponentQueryDefinition;
import de.unistuttgart.gropius.api.CreateIssueInput;
import de.unistuttgart.gropius.api.CreateIssuePayloadQuery;
import de.unistuttgart.gropius.api.CreateIssuePayloadQueryDefinition;
import de.unistuttgart.gropius.api.Issue;
import de.unistuttgart.gropius.api.IssueFilter;
import de.unistuttgart.gropius.api.IssuePageQuery;
import de.unistuttgart.gropius.api.IssuePageQueryDefinition;
import de.unistuttgart.gropius.api.IssueQuery;
import de.unistuttgart.gropius.api.IssueQueryDefinition;
import de.unistuttgart.gropius.api.MutationQuery;
import de.unistuttgart.gropius.api.MutationQueryDefinition;
import de.unistuttgart.gropius.api.NodeQuery;
import de.unistuttgart.gropius.api.NodeQueryDefinition;
import de.unistuttgart.gropius.api.Operations;
import de.unistuttgart.gropius.api.ProjectFilter;
import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.gropius.api.QueryQueryDefinition;
import de.unistuttgart.gropius.api.QueryQuery.ProjectsArguments;
import de.unistuttgart.gropius.api.QueryQuery.ProjectsArgumentsDefinition;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQuerier;
import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQueries;

public class Foo {
	@Test
	public void foo() throws IOException {
		// System.out.println(GropiusApiQueries.getLinkIssueMutation(new ID("foo"), new
		// ID("bar")).toString());

		LocalDateTime d = LocalDateTime.parse("2021-06-07T12:15:43.759Z", DateTimeFormatter.ISO_DATE_TIME);

		Instant then = d.toInstant(ZoneOffset.UTC);

		Instant now = Instant.now();

		Instant diff = now.minus(then.getEpochSecond(), ChronoUnit.SECONDS);

		long duration = diff.getEpochSecond();

		System.out.println(duration);
	}

	@Test
	public void fo2() throws IOException {
		File file = new File("src/test/resources/t2Process.bpmn2");
		System.out.println(FileUtils.readFileToString(file, StandardCharsets.UTF_8));

	}
	
	@Test
	public void qnode() throws IOException, InterruptedException {
		
		CloseIssueInput cii = new CloseIssueInput(new ID("fO"));
		
		// result
		IssueQueryDefinition iqd = (IssueQuery iq) -> {iq.title();}; 		
		CloseIssuePayloadQueryDefinition queryDef = (CloseIssuePayloadQuery cipq) -> {cipq.issue(iqd);};
		
		// mutation
		MutationQueryDefinition mqd = (MutationQuery mq) -> { mq.closeIssue(cii, queryDef);}; 
		
		System.out.println(Operations.mutation(mqd).toString());
		
		System.err.println(GropiusApiQueries.getCreateIssueMutation("componentId", "jsonBody", "title").toString());
		
	}
	
	@Test
	public void closeAllIssues() throws IOException, InterruptedException {
		GropiusApiQuerier queriere  = new GropiusApiQuerier("http://localhost:8080/api/");
		//Query query = queriere.queryQuery(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID("5ece9ebcc4ec5011")));
		
		Query query = queriere.queryQuery(GropiusApiQueries.getOpenIssueOnComponentQuery(new ID("5ece9ed4662c5013")));

		
		
		if (!query.getNode().getGraphQlTypeName().equals("Component")) {
			return;
		}
		
		List<Issue> bodies = ((Component) query.getNode()).getIssues().getNodes();
		Set<ID> ids = bodies.stream().map(i -> i.getId()).collect(Collectors.toSet());
		
		for (ID id : ids) {
			MutationQuery mq = closeQuery(id);
			System.err.println(mq);
			queriere.queryMutation(mq);
		}
	}
	
	public MutationQuery  closeQuery(ID id) {
		CloseIssueInput cii = new CloseIssueInput(id);
		IssueQueryDefinition iqueryDef = (IssueQuery iq) -> {};
		CloseIssuePayloadQueryDefinition queryDef = (CloseIssuePayloadQuery cipq) -> {cipq.issue(iqueryDef);};
		MutationQueryDefinition mqd = (MutationQuery mq) -> { mq.closeIssue(cii, queryDef);}; 
		
		return Operations.mutation(mqd);
	}
}
//2021-06-07T12:15:43.759Z
//1970-03-09T01:15:22.482896Z