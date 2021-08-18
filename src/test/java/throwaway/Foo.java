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
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.api.Component;
import de.unistuttgart.gropius.api.ComponentQuery;
import de.unistuttgart.gropius.api.ComponentQuery.IssuesArguments;
import de.unistuttgart.gropius.api.ComponentQuery.IssuesArgumentsDefinition;
import de.unistuttgart.gropius.api.ComponentQueryDefinition;
import de.unistuttgart.gropius.api.Issue;
import de.unistuttgart.gropius.api.IssueFilter;
import de.unistuttgart.gropius.api.IssuePageQuery;
import de.unistuttgart.gropius.api.IssuePageQueryDefinition;
import de.unistuttgart.gropius.api.IssueQuery;
import de.unistuttgart.gropius.api.IssueQueryDefinition;
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
		
		QueryQuery  qq = GropiusApiQueries.getOpenIssueOnComponentQuery(new ID("5ece9ebcc4ec5011"));
		
		Query q = new GropiusApiQuerier("http://localhost:8080/api").queryQuery(qq);
		
		Component c = (Component) q.getNode();
		
		List<Issue> issues = c.getIssues().getNodes();
		List<String> bodies = issues.stream().map(i -> i.getBody()).collect(Collectors.toList());
		
		String body = bodies.get(0);
		
		body = body.split("\\(")[1];
		body = body.split("\\)")[0];
		
		System.out.println(body);
		
	}
}
//2021-06-07T12:15:43.759Z
//1970-03-09T01:15:22.482896Z