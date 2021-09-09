package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.rest.ImportRequest;

@ContextConfiguration(classes = TestContext.class)
@DataMongoTest
@ActiveProfiles("test")
class ModelCreationServiceTest extends TestWithRepoAndMockServers {

	/**
	 * Successfully create a model.
	 *  
	 * @throws ModelCreationFailedException
	 * @throws IOException
	 */
	@Test
	public void creationTest() throws ModelCreationFailedException, IOException {
		long i = systemRepo.count();
		importService.createModel(request);
		assertEquals(i + 1, systemRepo.count());
	}

	/** 
	 * Do not create any model because of invalid gropius url.
	 * @throws IOException
	 */
	@Test
	public void creationTest_Fail_wrongGropiusUrl() throws IOException {
		{
			ImportRequest request = new ImportRequest(base + solomon, base + "foo", "t2-extended", "solomonEnvironment",
					"ressourceUri.saga", bpmn);
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
		{
			ImportRequest request = new ImportRequest(base + solomon, "", "t2-extended", "solomonEnvironment",
					"ressourceUri.saga", bpmn);
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
	}

	/**
	 * Do not create any model because of invalid solomon url.
	 * @throws IOException
	 */
	@Test
	public void creationTest_Fail_wrongSolomonUrl() throws IOException {
		{
			ImportRequest request = new ImportRequest(base + "foo", base + gropius, "t2-extended", "solomonEnvironment",
					"ressourceUri.saga", bpmn);
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
		{
			ImportRequest request = new ImportRequest("", base + gropius, "t2-extended", "solomonEnvironment",
					"ressourceUri.saga", bpmn);
			assertThrows(ModelCreationFailedException.class, () -> {
				importService.createModel(request);
			});
		}
	}
}
