package de.unistuttgart.ma.backend.importer.slo;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.Project;
import de.unistuttgart.gropius.slo.SloFactory;

/**
 * A {@code SolomonImporter} imports SLO rules from the Solomon tool.
 * 
 * It queries the Solomon tool for the SLO rules and transforms the response
 * into instances of the SLO ecore model. The importer needs an architecture to
 * attach the slo rules to. Therefore the architecture must be imported before
 * the SLO rules, or else the SLO rules can not be merged properly and will be
 * ignored.
 *
 */
public class SolomonImporter {

	private final de.unistuttgart.ma.saga.System model;
	private final SolomonApiQuerier querier;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** c.f. solomon/.../slo-rule.model.ts) */
	private final String environment;

	/**
	 * Create a new importer that imports SLO rules from the Solomon tool at
	 * {@code uri}.
	 * 
	 * @param uri         the uri of the Solomon tool
	 * @param environment environment to get rules for
	 * @param model       model of the system, must already contain the
	 *                    architecture.
	 */
	public SolomonImporter(String uri, String environment, de.unistuttgart.ma.saga.System model) {
		if (model == null) {
			throw new IllegalArgumentException("System model must not be null");
		}
		if (model.getArchitecture() == null) {
			throw new IllegalArgumentException(String.format(
					"System model %s is missing an architecture, cannot create a SolomonImporter with such a model",
					model.getId()));
		}
		this.querier = new SolomonApiQuerier(uri);
		this.environment = environment;
		this.model = model;
	}

	/**
	 * Get the Slo rules.
	 * 
	 * Query the Solomon tool for the slo rules of the given environment and parse
	 * the rules in the response to a model according to the SLo ecore model.
	 * 
	 * @return the SLO rules.
	 * @throws ModelCreationFailedException if the creation of the Slo rules part of
	 *                                      the model failed
	 */
	public Set<SloRule> parse() throws ModelCreationFailedException {
		try {
			Set<FlatSolomonRule> slorules = querier.querySolomon(environment);
			return parse(slorules);
		} catch (Exception e) {
			throw new ModelCreationFailedException("Could not import slos : " + e.getMessage(), e);
		}
	}

	/**
	 * Parse the SLO rules from the Solomon tool to SLO rules according to the SLO
	 * rule ecore model.
	 * 
	 * Parsing the rules also includes correctly linking them to other elements of
	 * the model. A SLO rule is attached to {@link Component} or to a
	 * {@link ComponentInterface}. If the locations of SLO rules from the Solomon
	 * tool do not match any known architecture elements, the rule is skipped.
	 * 
	 * @param flatRules the rules from the Solomon tool
	 * @return the SLO rules.
	 */
	private Set<SloRule> parse(Set<FlatSolomonRule> flatRules) {
		Set<SloRule> rules = new HashSet<>();
		for (FlatSolomonRule flatRule : flatRules) {
			// rule belongs to architecture of interest?
			Project project = model.getArchitecture();
			if (!project.getId().equals(flatRule.getGropiusProjectId())) {
				logger.debug(String.format("Skip SloRule %s because project does not match", flatRule.getName()));
				continue;
			}

			try {
				Component component = model.getComponentById(flatRule.getGropiusComponentId());

				for (ComponentInterface iface : component.getInterfaces()) {

					SloRule rule = SloFactory.eINSTANCE.createSloRule();

					rule.setGropiusComponentInterface(iface);
					rule.setGropiusComponent(component);
					rule.setGropiusProject(project);

					rule.setId(flatRule.getId());
					rule.setName(flatRule.getName());
					rule.setPeriod(flatRule.getPeriod());
					rule.setThreshold(flatRule.getThreshold());

					rule.setPresetOption(flatRule.getPreset());
					rule.setStatisticsOption(flatRule.getStatistic());
					rule.setComparisonOperator(flatRule.getComparisonOperator());

					rules.add(rule);

					logger.info(String.format("Parsed SloRule %s.", flatRule.getName()));
				}
			} catch (NoSuchElementException e) {
				logger.info(String.format("Skip SloRule %s because of %s.", flatRule.getName(),
						e.getClass().getCanonicalName()));
			}
		}
		return rules;
	}
}
