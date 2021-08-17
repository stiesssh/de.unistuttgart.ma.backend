package de.unistuttgart.ma.backend.importer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.eclipse.bpmn2.Process;
import org.junit.jupiter.api.Test;

import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.process.BPMNImporter;

public class BPMNImporterTest extends TestWithRepoAndMockServers {
	@Test
	public void test() throws ModelCreationFailedException, IOException {
		BPMNImporter importer = new BPMNImporter(bpmn);
		Process actual = importer.parse();
		
		assertNotNull(actual);
		assertFalse(actual.getFlowElements().isEmpty());
	}
}
