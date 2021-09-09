package de.unistuttgart.ma.backend.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.unistuttgart.gropius.Project;
import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.architecture.GropiusImporter;

/**
 * Tests for {@link GropiusImporter}.
 *
 */
public class GropiusImporterTest extends TestWithRepoAndMockServers {
	@Test
	public void test() throws ModelCreationFailedException, IOException {
		GropiusImporter importer = new GropiusImporter(base + gropius, "t2-extended");
		Project actual = importer.parse();

		assertNotNull(actual);
		assertNotNull(actual.getComponents());
		assertFalse(actual.getComponents().isEmpty());

		assertEquals("5e8cc17ed645a00c", actual.getId().toString());
	}
}
