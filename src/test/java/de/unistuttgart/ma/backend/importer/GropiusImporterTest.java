package de.unistuttgart.ma.backend.importer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.architecture.DataMapper;
import de.unistuttgart.ma.backend.importer.architecture.GropiusImporter;

public class GropiusImporterTest extends TestWithRepoAndMockServers {
	@Test
	public void test() throws ModelCreationFailedException, IOException {
		GropiusImporter importer = new GropiusImporter(base + gropius, "t2-extended");
		importer.parse();
		DataMapper mapper = DataMapper.getMapper();
		assertNotNull(mapper.getProjectByID(new ID(gropiusId)));
	}
}
