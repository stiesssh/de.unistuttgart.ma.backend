package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.rest.ImportRequest;
import de.unistuttgart.ma.saga.System;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
class SagaImporterTest extends TestWithRepoAndMockServers {

	protected System loadSystem(String file) throws IOException {
		String xml = Files.readString(Paths.get("src/test/resources/", file), StandardCharsets.UTF_8);

		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());

		// create new resource, other wise we wont load, but instead just reuse stuff
		// from the previous parsing.
		Resource recource = set.createResource(URI.createPlatformResourceURI("foo.saga", false));
		recource.load(inputStream, null);

		for (EObject eObject : recource.getContents()) {
			if (eObject instanceof System) {
				systemRepoProxy.save((System) eObject);
			}
		}

		return systemRepoProxy.findById(systemId);
	}

	@Test
	void parseEmptySystemTest() throws IOException {
		String filename = "empty.saga";

		loadSystem(filename);
		assertEquals(1, systemRepo.count());

		de.unistuttgart.ma.saga.System actual = systemRepoProxy.findById("60fa9cadc736ff6357a89a9b");

		assertNotNull(actual);
		assertNotNull(actual.getArchitecture());

		assertEquals(filename, actual.eResource().getURI().segment(actual.eResource().getURI().segmentCount() - 1));
	}

	@Test
	void parseT2BaseSystemTest() throws IOException {
		loadSystem();
		String filename = "t2-base.saga";

		loadSystem("t2_base_saga.saga");
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
	public void creationTest() throws ModelCreationFailedException, IOException {
		long i = systemRepo.count();
		importService.createModel(request);
		assertEquals(i + 1, systemRepo.count());
	}

	@Test
	public void creationTest_Fail_wrongGropiusUrl() throws IOException {
		{
			ImportRequest request = new ImportRequest(base + solomon, base + "foo",
					"src/test/resources/t2Process.bpmn2", "t2-extended", "solomonEnvironment", "ressourceUri.saga");
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
		{
			ImportRequest request = new ImportRequest(base + solomon, "", "src/test/resources/t2Process.bpmn2",
					"t2-extended", "solomonEnvironment", "ressourceUri.saga");
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
	}
	
	@Test
	public void creationTest_Fail_wrongSolomonUrl() throws IOException {
		{
			ImportRequest request = new ImportRequest(base + "foo", base + gropius, 
					"src/test/resources/t2Process.bpmn2", "t2-extended", "solomonEnvironment", "ressourceUri.saga");
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
		{
			ImportRequest request = new ImportRequest("", base + gropius, "src/test/resources/t2Process.bpmn2",
					"t2-extended", "solomonEnvironment", "ressourceUri.saga");
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
	}
}
