package de.unistuttgart.ma.backend.importer.architecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.gropius.api.Query;
import de.unistuttgart.gropius.api.QueryQuery;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.Project;

/**
 * A {@code GropiusImporter} imports an architecture from Gropius.
 * 
 * It queries the Gropius backend for the architecture and transforms the
 * response into an instance of the Gropius ecore model.
 * 
 */
public class GropiusImporter {

	private final GropiusApiQuerier apiQuerier;
	private final String projectName;

	private final DataMapper mapper;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Create a new importer to import the project with the given name from tbe
	 * gropius back end at the given uri.
	 * 
	 * @param uri         uri of the gropius backend
	 * @param projectName name of the project
	 */
	public GropiusImporter(String uri, String projectName) {
		if (projectName == null) {
			throw new IllegalArgumentException("projectName is null");
		}
		if (uri == null) {
			throw new IllegalArgumentException("uri is null");
		}
		this.apiQuerier = new GropiusApiQuerier(uri);
		this.projectName = projectName;
		this.mapper = new DataMapper();
	}

	/**
	 * Get the architecture.
	 * 
	 * Queries the Gropius back end for the architecture and transforms the reply
	 * into a model according to the Gropius Ecore model.
	 * 
	 * @return the architecture 
	 * @throws ModelCreationFailedException if the creation of the architecture part
	 *                                      of the model failed
	 */
	public Project parse() throws ModelCreationFailedException {
		QueryQuery queryQuery = GropiusApiQueries.getSingleProjectQuery(projectName);
		logger.debug(String.format("Query for %s.", queryQuery.toString()));
		try {
			Query query = apiQuerier.queryQuery(queryQuery);
			return parse(query);
		} catch (Exception e) { // just catch everything :x
			throw new ModelCreationFailedException("could not parse project : " + e.getMessage(), e);
		}
	}

	/**
	 * Parse the content of the {@link Query} to an architecture model according to
	 * the Gropius Ecore model.
	 * 
	 * @param response query response from the gropius backend
	 * @return the architecture
	 * @throws ModelCreationFailedException if the creation of the architecture part
	 *                                      of the model failed
	 */
	private Project parse(Query response) throws ModelCreationFailedException {
		assert(response != null);
		if (response.getProjects().getNodes().size() > 1) {
			throw new ModelCreationFailedException("queried for a single project but got multiple", null);
		}
		if (response.getProjects().getNodes().size() < 1) {
			throw new ModelCreationFailedException("queried for a single project but found none", null);
		}
		logger.debug(String.format("Received 1 project from gropius backend."));

		// add the projects
		de.unistuttgart.gropius.api.Project gropiusProject = response.getProjects().getNodes().get(0);
		Project project = mapper.getEcoreProject(gropiusProject);

		// parse all components with provided interfaces and connect them.
		for (de.unistuttgart.gropius.api.Component gropiusComponent : gropiusProject.getComponents().getNodes()) {

			Component component = mapper.getEcoreComponent(gropiusComponent);
			project.getComponents().add(component);

			// provided
			for (de.unistuttgart.gropius.api.ComponentInterface gropiusInterface : gropiusComponent.getInterfaces()
					.getNodes()) {
				ComponentInterface face = mapper.getEcoreInterface(gropiusInterface);
				component.getInterfaces().add(face);
			}
			// consumed
			for (de.unistuttgart.gropius.api.ComponentInterface gropiusInterface : gropiusComponent
					.getConsumedInterfaces().getNodes()) {
				ComponentInterface face = mapper.getEcoreInterface(gropiusInterface);
				component.getConsumedInterfaces().add(face);
				face.getConsumedBy().add(component);

			}
		}

		logger.debug(String.format("Parsed one project with id %s.", project.getId()));
		return project;
	}
}
