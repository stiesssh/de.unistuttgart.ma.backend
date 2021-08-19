package de.unistuttgart.ma.backend.importer.slo;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.exceptions.ModelCreationFailedException;
import de.unistuttgart.ma.backend.importer.architecture.DataMapper;
import de.unistuttgart.gropius.Component;
import de.unistuttgart.gropius.ComponentInterface;
import de.unistuttgart.gropius.slo.SloFactory;

public class SolomonImporter {

	private final DataMapper gropiusmapper;
	private final SolomonApiQuerier querier;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	// TODO : this is a DeploymentEnvironment (currently either 'aws' or
	// 'kubernetes', vgl. solomon/**/slo-rule.model.ts)
	private final String environmentParameter;

	public SolomonImporter(String uri, String environmentParameter) {
		this.querier = new SolomonApiQuerier(uri);
		this.environmentParameter = environmentParameter;
		this.gropiusmapper = DataMapper.getMapper();
	}

	public Set<SloRule> parse() throws ModelCreationFailedException {
		try {
			Set<FlatSolomonRule> slorules = querier.querySolomon(environmentParameter);
			return parseSolomon(slorules);
		} catch (Exception e) {
			throw new ModelCreationFailedException("Could not import slos : " + e.getMessage(), e);
		}
	}

	private Set<SloRule> parseSolomon(Set<FlatSolomonRule> flatRules) {
		Set<SloRule> rules = new HashSet<>();
		for (FlatSolomonRule flatRule : flatRules) {
			try {
				Component component = gropiusmapper.getComponentByID(new ID(flatRule.getGropiusComponentId()));
				for (ComponentInterface iface : component.getInterfaces()) {

					SloRule rule = SloFactory.eINSTANCE.createSloRule();

					rule.setGropiusComponentInterface(iface);
					rule.setGropiusComponent(component);
					rule.setGropiusProject(gropiusmapper.getProjectByID(new ID(flatRule.getGropiusProjectId())));

					rule.setId(flatRule.getId());
					rule.setName(flatRule.getName());
					rule.setPeriod(flatRule.getPeriod());
					rule.setThreshold(flatRule.getThreshold());

					rule.setPresetOption(flatRule.getPreset());
					rule.setStatisticsOption(flatRule.getStatistic());
					rule.setComparisonOperator(flatRule.getComparisonOperator());

					rules.add(rule);
					
					logger.info(String.format("Create SloRule %s.", flatRule.getName()));
				}
			} catch (NoSuchElementException e) {
				logger.info(String.format("Skip SloRule %s because of %s.", flatRule.getName(), e.getClass().getCanonicalName()));
			}
		}
		return rules;
	}
}
