package de.unistuttgart.ma.backend.importer.architecture;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.Project;

/**
 * A {@code DataMapper} maps commponents and interface from the graphql schema
 * of gropius to their ecore representation.
 * 
 * The mapper creates new projects, interface and components where required but
 * does not set all references in between, just in case there are cycles. Make
 * sure to set the missing references yourself.
 * 
 * It is a singleton, because everyone should use the same mapper.
 * 
 * @author maumau
 *
 */
public class DataMapper {

	private Map<ID, Project> projectMap;
	private Map<ID, Component> componentMap;
	private Map<ID, ComponentInterface> interfaceMap;

	GropiusFactory factory = GropiusFactory.eINSTANCE;

	private static DataMapper instance;

	/**
	 * Get the {@code DataMapper}.
	 * 
	 * @return the mapper instance
	 */
	public static DataMapper getMapper() {
		if (instance != null) {
			return instance;
		}
		instance = new DataMapper();
		return instance;
	}

	private DataMapper() {
		componentMap = new HashMap<>();
		interfaceMap = new HashMap<>();
		projectMap = new HashMap<>();
	}

	/**
	 * Get the {@link de.unistuttgart.gropius.Component} that matches the given
	 * {@link de.unistuttgart.gropius.api.Component}.
	 * 
	 * If there is not yet a match, a new ecore component is created. Its provided
	 * and consumed interfaces are NOT set, because the interface might not yet be
	 * known.
	 * 
	 * @param component a graphql component
	 * @return a ecore component
	 * @throws MappingFailedException
	 */
	public Component getEcoreComponent(de.unistuttgart.gropius.api.Component component) {
		if (component == null || component.getId() == null || component.getName() == null) {
			throw new IllegalArgumentException("Cannot map component.");
		}
		ID id = component.getId();
		if (componentMap.containsKey(id)) {
			return componentMap.get(id);
		}

		Component result = factory.createComponent();
		result.setName(component.getName());
		result.setId(component.getId().toString());
		componentMap.put(id, result);
		return result;
	}

	/**
	 * Get the {@link de.unistuttgart.gropius.ComponentInterface} that matches the
	 * given {@link de.unistuttgart.gropius.api.ComponentInterface}.
	 * 
	 * If there is not yet a match, a new ecore component interface is created. Its
	 * provider component is set, however the consuming components are not.
	 * 
	 * @param compInterface a graphql component interface
	 * @return a ecore component interface
	 */
	public ComponentInterface getEcoreInterface(de.unistuttgart.gropius.api.ComponentInterface compInterface) {
		if (compInterface == null || compInterface.getId() == null || compInterface.getName() == null
				|| compInterface.getComponent() == null) {
			throw new IllegalArgumentException("Cannot map component interface.");
		}
		ID id = compInterface.getId();
		if (interfaceMap.containsKey(id)) {
			return interfaceMap.get(id);
		}

		ComponentInterface result = factory.createComponentInterface();
		result.setName(compInterface.getName());
		result.setId(compInterface.getId().toString());
		result.setComponent(getEcoreComponent(compInterface.getComponent()));
		interfaceMap.put(id, result);
		return result;
	}

	/**
	 * Get the {@link de.unistuttgart.gropius.Project} that matches the given
	 * {@link de.unistuttgart.gropius.api.Project}.
	 * 
	 * If there is not yet a match, a new ecore project is created. Only the name
	 * and id are set.
	 * 
	 * @param project a graphql project
	 * @return a ecore project
	 */
	public Project getEcoreProject(de.unistuttgart.gropius.api.Project project) {
		if (project == null || project.getId() == null || project.getName() == null) {
			throw new IllegalArgumentException("Cannot map project.");
		}
		ID id = project.getId();
		if (projectMap.containsKey(id)) {
			return projectMap.get(id);
		}

		Project result = factory.createProject();
		result.setName(project.getName());
		result.setId(project.getId().toString());
		projectMap.put(id, result);
		return result;
	}

	/**
	 * Get a {@link de.unistuttgart.gropius.Component} by id.
	 * 
	 * @param id ID of the component
	 * @return the ecore component
	 */
	public Component getComponentByID(ID id) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		if (componentMap.containsKey(id)) {
			return componentMap.get(id);
		}
		throw new NoSuchElementException();
	}

	/**
	 * Get a {@link de.unistuttgart.gropius.ComponentInterface} by id.
	 * 
	 * @param id ID of the component interface
	 * @return the ecore component interface
	 */
	public ComponentInterface getComponentInterfaceByID(ID id) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		if (interfaceMap.containsKey(id)) {
			return interfaceMap.get(id);
		}
		throw new NoSuchElementException();
	}

	/**
	 * Get a {@link de.unistuttgart.gropius.Project} by id.
	 * 
	 * @param id ID of the project
	 * @return the ecore project
	 */
	public Project getProjectByID(ID id) {
		if (id == null) {
			throw new IllegalArgumentException("ID is null");
		}
		if (projectMap.containsKey(id)) {
			return projectMap.get(id);
		}
		throw new NoSuchElementException();
	}
}
