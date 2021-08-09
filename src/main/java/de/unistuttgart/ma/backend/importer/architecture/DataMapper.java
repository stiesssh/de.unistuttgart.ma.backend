package de.unistuttgart.ma.backend.importer.architecture;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.GropiusFactory;
import de.unistuttgart.gropius.Project;

public class DataMapper {
	
	private Map<ID, Project> projectMap;
	private Map<ID, Component> componentMap;
	private Map<ID, ComponentInterface> interfaceMap;

	GropiusFactory factory = GropiusFactory.eINSTANCE; 
	
	private static DataMapper instance; 
	
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
	 * component with name set but interfaces not set
	 * @param component
	 * @return
	 */
	public Component getEcoreComponent(de.unistuttgart.gropius.api.Component component) {
		ID id = component.getId();
		if(componentMap.containsKey(id)) {
			return componentMap.get(id);
		} 
		
		Component result = factory.createComponent();
		result.setName(component.getName());
		result.setId(component.getId().toString());
		result.setDescription(component.getDescription());
		componentMap.put(id, result);
		return result;
	}
	
	/**
	 * interface with name & provider set
	 * @param compInterface
	 * @return
	 */
	public ComponentInterface getEcoreInterface(de.unistuttgart.gropius.api.ComponentInterface compInterface) {
		ID id = compInterface.getId();
		if(interfaceMap.containsKey(id)) {
			return interfaceMap.get(id);
		}
		
		ComponentInterface result = factory.createComponentInterface();
		result.setName(compInterface.getName());
		result.setId(compInterface.getId().toString());
		result.setDescription(compInterface.getDescription());
		result.setComponent(getEcoreComponent(compInterface.getComponent()));
		interfaceMap.put(id, result);
		return result;
	}
	
	/**
	 * project. 
	 * @param component
	 * @return
	 */
	public Project getEcoreProject (de.unistuttgart.gropius.api.Project project) {
		ID id = project.getId();
		if(projectMap.containsKey(id)) {
			return projectMap.get(id);
		} 
		
		Project result = factory.createProject();
		result.setName(project.getName());
		result.setId(project.getId().toString());
		projectMap.put(id, result);
		return result;
	}
	
	public Component getComponentByID(ID id) {
		if(componentMap.containsKey(id)) {
			return componentMap.get(id);
		}
		throw new NoSuchElementException();
	}
	
	public ComponentInterface getComponentInterfaceByID(ID id) {
		if(interfaceMap.containsKey(id)) {
			return interfaceMap.get(id);
		}
		throw new NoSuchElementException();
	}
	
	public Project getProjectByID(ID id) {
		if(projectMap.containsKey(id)) {
			return projectMap.get(id);
		}
		throw new NoSuchElementException();
	}
}
