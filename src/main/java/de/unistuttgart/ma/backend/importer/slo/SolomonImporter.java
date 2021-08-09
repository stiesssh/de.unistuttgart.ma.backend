package de.unistuttgart.ma.backend.importer.slo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.shopify.graphql.support.ID;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.backend.importer.architecture.DataMapper;
import de.unistuttgart.gropius.slo.SloFactory;

public class SolomonImporter implements SloImporter {
	
	private final DataMapper gropiusmapper;
	
	private final SolomonApiQuerier querier;
	
	// TODO : this is a DeploymentEnvironment (currently either 'aws' or 'kubernetes', vgl. solomon/**/slo-rule.model.ts)
	private final String environmentParameter; 

	public SolomonImporter(String uri, String environmentParameter) {
		this.querier = new SolomonApiQuerier(uri);
		this.environmentParameter = environmentParameter;
		this.gropiusmapper = DataMapper.getMapper();
	}
	
	@Override
	public Set<SloRule> parse() {
		try {
			Set<SloFlatRule> slorules = querier.query(environmentParameter); 
			return parse(slorules);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private Set<SloRule> parse(Set<SloFlatRule> flatRules) {
		Set<SloRule> rules = new HashSet<>();
		for (SloFlatRule flatRule : flatRules) {
			
			SloRule rule = SloFactory.eINSTANCE.createSloRule(); 
			
			rule.setId(flatRule.getId());
			rule.setName(flatRule.getName());
			rule.setPeriod(0);		//TODO
			rule.setThreshold(0);	//TODO
			
			rule.setGropiusProject(gropiusmapper.getProjectByID(new ID(flatRule.getGropiusProjectId())));
			rule.setGropiusComponent(gropiusmapper.getComponentByID(new ID(flatRule.getGropiusComponentId())));
			rule.setGropiusComponentInterface(gropiusmapper.getComponentInterfaceByID(new ID(flatRule.getGropiusComponentInterfaceId())));
			
			rules.add(rule);
		}
		return rules;
	}
}
