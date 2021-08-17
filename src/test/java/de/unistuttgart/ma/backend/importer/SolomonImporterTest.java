package de.unistuttgart.ma.backend.importer;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.architecture.GropiusImporter;
import de.unistuttgart.ma.backend.importer.slo.SolomonImporter;

/**
 * 
 * @author maumau
 *
 */
public class SolomonImporterTest extends TestWithRepoAndMockServers {
	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		GropiusImporter importer = new GropiusImporter(base + gropius, "t2-extended");
		try {
			importer.parse();
		} catch (ModelCreationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws ModelCreationFailedException, IOException {
		SolomonImporter importer = new SolomonImporter(base + solomon, solomonEnvironment);
		Set<SloRule> rules = importer.parse();
		
		assertFalse(rules.isEmpty());
		
		// TODO : more assert
	}
}
