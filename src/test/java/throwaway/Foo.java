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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

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
}
//2021-06-07T12:15:43.759Z
//1970-03-09T01:15:22.482896Z