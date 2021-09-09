package de.unistuttgart.ma.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.api.Component;
import de.unistuttgart.gropius.api.ComponentInterface;
import de.unistuttgart.gropius.api.Project;
import de.unistuttgart.ma.backend.importer.architecture.DataMapper;

/**
 * Test for {@link DataMapper}.
 */
public class DataMapperTest {

	DataMapper mapper;
	
	@BeforeEach
	void setUp() {
		mapper = new DataMapper();
	}
	
	@Test
	void testGetProject() {
		Project expected = new Project(new ID("id"));
		expected.setName("name");
		
		de.unistuttgart.gropius.Project actual = mapper.getEcoreProject(expected);
		assertNotNull(actual);
		assertNotNull(actual.getId());
		assertEquals(expected.getId().toString(), actual.getId());
		
		assertNotNull(actual.getName());
		assertEquals(expected.getName(), actual.getName());
	}
	
	@Test
	void testFailGetProject() {
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreProject(null));
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreProject(new Project()));
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreProject(new Project(new ID("id"))));
		
		assertThrows(IllegalArgumentException.class, () -> mapper.getProjectByID(null));
		assertThrows(NoSuchElementException.class, () -> mapper.getProjectByID(new ID("id")));
	}
	
	
	@Test
	void testGetComponent() {
		Component expected = new Component(new ID("id"));
		expected.setName("name");
		
		de.unistuttgart.gropius.Component actual = mapper.getEcoreComponent(expected);
		assertNotNull(actual);
		assertNotNull(actual.getId());
		assertEquals(expected.getId().toString(), actual.getId());
		
		assertNotNull(actual.getName());
		assertEquals(expected.getName(), actual.getName());
	}
	
	@Test
	void testFailGetComponent() {
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreComponent(null));
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreComponent(new Component()));
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreComponent(new Component(new ID("id"))));
		
		assertThrows(IllegalArgumentException.class, () -> mapper.getComponentByID(null));
		assertThrows(NoSuchElementException.class, () -> mapper.getComponentByID(new ID("id")));
	}
	
	@Test
	void testGetComponentInterface() {
		ComponentInterface expected = new ComponentInterface(new ID("id"));
		expected.setName("name");
		
		Component component = new Component(new ID("id"));
		component.setName("name");
		expected.setComponent(component);
		
		de.unistuttgart.gropius.ComponentInterface actual = mapper.getEcoreInterface(expected);
		assertNotNull(actual);
		assertNotNull(actual.getId());
		assertEquals(expected.getId().toString(), actual.getId());
		
		assertNotNull(actual.getName());
		assertEquals(expected.getName(), actual.getName());
	}
	
	@Test
	void testFailGetInterface() {
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreInterface(null));
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreInterface(new ComponentInterface()));
		assertThrows(IllegalArgumentException.class, () -> mapper.getEcoreInterface(new ComponentInterface(new ID("id"))));
		
		assertThrows(IllegalArgumentException.class, () -> mapper.getComponentInterfaceByID(null));
		assertThrows(NoSuchElementException.class, () -> mapper.getComponentInterfaceByID(new ID("id")));
	}
}
