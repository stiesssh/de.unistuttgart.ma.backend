package de.unistuttgart.ma.backend.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.TestWithRepoAndMockServers;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.slo.FlatSolomonRule;
import de.unistuttgart.ma.backend.importer.slo.SolomonImporter;
import de.unistuttgart.ma.saga.SagaFactory;
import de.unistuttgart.ma.saga.System;

/**
 * 
 * @author maumau
 *
 */
public class SolomonImporterTest extends TestWithRepoAndMockServers {

	@Test
	public void test_parseRules() throws ModelCreationFailedException, IOException {
		SolomonImporter importer = new SolomonImporter(base + solomon, solomonEnvironment, getSystem());
		Set<SloRule> rules = importer.parse();
		
		assertFalse(rules.isEmpty());
		assertEquals(expectedRules.size(), rules.size());
		
		for (SloRule actual : rules) {
			assert(expectedRules.stream().map(r -> r.getName()).collect(Collectors.toSet()).contains(actual.getName()));
			FlatSolomonRule expected = expectedRules.stream().filter(r -> {return r.getName().equals(actual.getName());}).findFirst().get();
			
			assertEquals(expected.getComparisonOperator(), actual.getComparisonOperator());
			assertEquals(expected.getGropiusProjectId(), actual.getGropiusProject().getId().toString());
			assertEquals(expected.getGropiusComponentId(), actual.getGropiusComponent().getId().toString());
			//assertEquals(expected.getGropius...Id(), actual.getGropiusComponent().getId().toString());
			
		}
		
		
	}
	
	@Test
	public void test_failNoSystem() throws ModelCreationFailedException, IOException {
		assertThrows(IllegalArgumentException.class, () -> new SolomonImporter(base + solomon, solomonEnvironment, null));
	}

	@Test
	public void test_failNoArch() throws ModelCreationFailedException, IOException {
		
		System noArch = SagaFactory.eINSTANCE.createSystem();
		noArch.setId("noarch");
		noArch.setName("noarch");

		assertThrows(IllegalArgumentException.class, () -> new SolomonImporter(base + solomon, solomonEnvironment, noArch));
	}
	
	@Test
	public void test_NoParse() throws ModelCreationFailedException, IOException {
		
		System otherArch = getArchOnlySystem();
		
		SolomonImporter importer = new SolomonImporter(base + solomon, solomonEnvironment, otherArch);
		assertTrue(importer.parse().isEmpty());
	}
}
