package de.unistuttgart.ma.backend.importer.architecture;

import java.io.IOException;

import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.Project;

/**
 * 
 * TODO : change flow to this : get projects by name -> display them, make user
 * choose one, becaus enames are not unique and i cant not query by id...
 * apparently?
 * 
 * @author maumau
 *
 */
public class GropiusImporter {

	private final GropiusApiQuerier manager;
	private final String projectName;

	private final DataMapper mapper;

	public GropiusImporter(String uri, String projectName) {
		super();
		this.manager = new GropiusApiQuerier(uri);
		this.projectName = projectName;
		this.mapper = DataMapper.getMapper();
	}

	public Project parse() throws ModelCreationFailedException {
		QueryQuery queryQuery = GropiusApiQueries.getSingleProjectQuery(projectName);
		System.out.println(queryQuery.toString());
		try {
			Query query = manager.queryQuery(queryQuery);
			return parse(query);
		} catch (Exception e) { // just catch everything :x
			throw new ModelCreationFailedException("could not parse project : " + e.getMessage(), e);			
		}
	}

	private Project parse(Query response) throws ModelCreationFailedException {

		if (response.getProjects().getNodes().size() > 1) {
			throw new ModelCreationFailedException("queried for a single project but got multiple", null);
		}
		if (response.getProjects().getNodes().size() < 1) {
			throw new ModelCreationFailedException("queried for a single project but found none", null);
		}

		// add the projects
		de.unistuttgart.gropius.api.Project gropiusProject = response.getProjects().getNodes().get(0);

		Project project = mapper.getEcoreProject(gropiusProject);

		// parse all components with provided interfaces
		for (de.unistuttgart.gropius.api.Component gropiusComponent : gropiusProject.getComponents().getNodes()) {

			Component component = mapper.getEcoreComponent(gropiusComponent);
			project.getComponents().add(component);

			// provided
			for (de.unistuttgart.gropius.api.ComponentInterface gropiusInterface : gropiusComponent.getInterfaces().getNodes()) {
				ComponentInterface face = mapper.getEcoreInterface(gropiusInterface);
				component.getInterfaces().add(face);
			}
			// consumed
			for (de.unistuttgart.gropius.api.ComponentInterface gropiusInterface : gropiusComponent.getConsumedInterfaces()
					.getNodes()) {
				ComponentInterface face = mapper.getEcoreInterface(gropiusInterface);
				component.getConsumedInterfaces().add(face);
				face.getConsumedBy().add(component);

			}
		}

		return project;
	}
}
