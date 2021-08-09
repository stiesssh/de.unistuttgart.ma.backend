package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
class SagaImporterTest extends TestWithRepoAndMockServers {

	@Test
	void parseEmptySystemTest() throws IOException {
		String filename = "empty.saga";
		String xml = Files.readString(Paths.get("src/test/resources/", filename), StandardCharsets.UTF_8);

		importService.parse(xml);
		assertEquals(1, systemRepo.count());

		de.unistuttgart.ma.saga.System actual = systemRepoProxy.findById("60fa9cadc736ff6357a89a9b");

		assertNotNull(actual);
		assertNotNull(actual.getArchitecture());

		assertEquals(filename, actual.eResource().getURI().segment(actual.eResource().getURI().segmentCount() - 1));
	}

	@Test
	void parseT2BaseSystemTest() throws IOException {
		loadSystem();
		String xml = Files.readString(Paths.get("src/test/resources/", "t2_base_saga.saga"), StandardCharsets.UTF_8);
		String filename = "t2-base.saga";

		importService.parse(xml);
		assertEquals(1, systemRepo.count());

		de.unistuttgart.ma.saga.System actual = systemRepoProxy.findById("60fa9cadc736ff6357a89a9b");

		assertNotNull(actual);
		assertNotNull(actual.getArchitecture());

		assertEquals(filename, actual.eResource().getURI().segment(actual.eResource().getURI().segmentCount() - 1));
		// or:
		assertEquals(URI.createPlatformResourceURI(filename, false).toString(), actual.eResource().getURI().toString());

		// TODO : assert the system :x
	}

	@Test
	void creationTest() throws IOException, InterruptedException {
		HttpClient httpClient = HttpClient.newBuilder().build();
		{
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET()
					.uri(java.net.URI.create("http://localhost:" + port + "/a"));

			HttpResponse<String> response = httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
			System.out.println(response.body());
		}
		{
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET()
					.uri(java.net.URI.create("http://localhost:" + port + "/b"));

			HttpResponse<String> response = httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
			System.out.println(response.body());
		}
		
		importService.createModel(request);
	}
}
