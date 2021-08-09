package de.unistuttgart.ma.backend.importer.slo;
/**
 * 
 * based on (yesterdays version of) : 
 * https://github.com/ccims/automated-sla-issue-management/blob/refactoring/solomon-models/src/slo-rule.model.ts
 * 
 * !! this might be the endpoint i'm searching for: 
 * https://github.com/ccims/automated-sla-issue-management/blob/8d36340639faf534a399fc9b0b4fa3dea729fcf4/solomon-backend/src/app.controller.ts#L12
 * 
 * TODO clarify :
 * - is there *any* endpoint where i can grab those rules? 'cause i will hereby assume that there is :x
 * - what do all those field even mean ? o_O
 * 
 *   
 * @author maumau
 *
 */
public class SloFlatRule {
	
	private String gropiusProjectId;
	private String gropiusComponentId;
	private String gropiusComponentInterfaceId;
	 
	private String id; 			// corresponds to AlarmArn in CW, can be a generated id for Prometheus (eh??) 
	private String name; 		// display name of the rule, provided by user
	private String description;	// displayed description of the rule, provided by user 
	
//	private DeploymentEnvironment deploymentEnvironment;
	private PresetOptions presetOptions;
//	private MetricOptions metricOptions;
//	private ComparisonOperator comparisonOperator;
//	private StatisticsOption statisticsOption;
//	
//	// the enums...
//	public enum DeploymentEnvironment { AWS, KUBERNETES }
	public enum PresetOptions { AVAILABILITY, RESPONSE_TIME, CUSTOM }
//	public enum MetricOptions { PROBE_SUCCESS, RESPONSE_TIME }
//	public enum ComparisonOperator { GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, EQUAL, NOT_EQUAL }
//	public enum StatisticsOption { AVG, RATE, SAMPLE_COUNT, SUM, MINIMUM, MAXIMUM }
    
	public SloFlatRule(String gropiusProjectId, String gropiusComponentId, String gropiusComponentInterfaceId, String id, String name, String description) {
		super();
		this.gropiusProjectId = gropiusProjectId;
		this.gropiusComponentId = gropiusComponentId;
		this.gropiusComponentInterfaceId = gropiusComponentInterfaceId;
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getGropiusProjectId() {
		return gropiusProjectId;
	}
	public void setGropiusProjectId(String gropiusProjectId) {
		this.gropiusProjectId = gropiusProjectId;
	}
	public String getGropiusComponentId() {
		return gropiusComponentId;
	}
	public void setGropiusComponentId(String gropiusComponentId) {
		this.gropiusComponentId = gropiusComponentId;
	}
	public String getGropiusComponentInterfaceId() {
		return gropiusComponentInterfaceId;
	}
	public void setGropiusComponentInterfaceId(String gropiusComponentInterfaceId) {
		this.gropiusComponentInterfaceId = gropiusComponentInterfaceId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
//	public DeploymentEnvironment getDeploymentEnvironment() {
//		return deploymentEnvironment;
//	}
//	public void setDeploymentEnvironment(DeploymentEnvironment deploymentEnvironment) {
//		this.deploymentEnvironment = deploymentEnvironment;
//	}
	public PresetOptions getPresetOptions() {
		return presetOptions;
	}
	public void setPresetOptions(PresetOptions presetOptions) {
		this.presetOptions = presetOptions;
	}
//	public MetricOptions getMetricOptions() {
//		return metricOptions;
//	}
//	public void setMetricOptions(MetricOptions metricOptions) {
//		this.metricOptions = metricOptions;
//	}
//	public ComparisonOperator getComparisonOperator() {
//		return comparisonOperator;
//	}
//	public void setComparisonOperator(ComparisonOperator comparisonOperator) {
//		this.comparisonOperator = comparisonOperator;
//	}
//	public StatisticsOption getStatisticsOption() {
//		return statisticsOption;
//	}
//	public void setStatisticsOption(StatisticsOption statisticsOption) {
//		this.statisticsOption = statisticsOption;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((gropiusComponentId == null) ? 0 : gropiusComponentId.hashCode());
		result = prime * result + ((gropiusProjectId == null) ? 0 : gropiusProjectId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((presetOptions == null) ? 0 : presetOptions.hashCode());
		return result;
	}
}
