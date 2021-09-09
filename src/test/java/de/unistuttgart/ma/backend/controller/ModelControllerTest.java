package de.unistuttgart.ma.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.app.ModelController;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;

/**
 * Tests for {@link ModelController}.
 */
public class ModelControllerTest extends TestWithRepoAndMockServers {

	String uri;

	ModelController controller;
	
	String expected;
	

	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		uri = "http://localhost:" + port + gropius;
		
		controller = new ModelController(modelService);
		
		try {
			expected = Files.readString(Paths.get("src/test/resources/", "t2_base_saga.saga"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Could not set up test.");
		}
	}
	

	@Test
	public void testGetModel() {
		loadSystem();
		String actual = controller.getModel(systemId);
		
	}
	
	@Test
	public void testFailGetModel() {
		assertThrows(NoSuchElementException.class, () -> controller.getModel("missing"));
	}
	
	@Test
	public void testcreateModel() throws ModelCreationFailedException {
		controller.createModel(request);
		
	}
	
	@Test
	public void testUpdateModel() {
		controller.updateModel(expected, systemId);
	}
}
