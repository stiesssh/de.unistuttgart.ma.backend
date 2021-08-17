package de.unistuttgart.ma.backend.importer.slo;

/**
	 * 
	 * A SloRule for the FakeSolomon
	 * based on : 
	 * https://github.com/ccims/automated-sla-issue-management/blob/refactoring/solomon-models/src/slo-rule.model.ts
	 *   
	 * @author maumau
	 *
	 */
public class FlatSloRule {
	private String gropiusProjectId;
	private String gropiusComponentId;
	private String gropiusComponentInterfaceId;
	 
	private String id; 			// corresponds to AlarmArn in CW, can be a generated id for Prometheus (eh??) 
	private String name; 		// display name of the rule, provided by user
	private String description;	// displayed description of the rule, provided by user 
	
	private PresetOption presetOption;
//	private MetricOptions metricOptions;
	private ComparisonOperator comparisonOperator;
	private StatisticsOption statisticsOption;
	

    private double period; // evaluation period in seconds, e.g. 86400
    private double threshold; // number against which to measure


	public enum PresetOption { AVAILABILITY, RESPONSE_TIME, CUSTOM }
//	public enum MetricOptions { PROBE_SUCCESS, RESPONSE_TIME }
	public enum ComparisonOperator { GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, EQUAL, NOT_EQUAL }
	public enum StatisticsOption { AVG, RATE, SAMPLE_COUNT, SUM, MINIMUM, MAXIMUM }
    
	public FlatSloRule(String gropiusProjectId, String gropiusComponentId, String gropiusComponentInterfaceId, String id, double threshold, double period) {
		super();
		this.gropiusProjectId = gropiusProjectId;
		this.gropiusComponentId = gropiusComponentId;
		this.gropiusComponentInterfaceId = gropiusComponentInterfaceId;
		this.id = id;
		this.name = id +  "_name";
		this.description = id+  "desc";
		this.threshold = threshold;
		this.period = period;
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
	public PresetOption getPresetOption() {
		return presetOption;
	}
	public void setPresetOption(PresetOption presetOption) {
		this.presetOption = presetOption;
	}
//	public MetricOptions getMetricOptions() {
//		return metricOptions;
//	}
//	public void setMetricOptions(MetricOptions metricOptions) {
//		this.metricOptions = metricOptions;
//	}
	public ComparisonOperator getComparisonOperator() {
		return comparisonOperator;
	}
	public void setComparisonOperator(ComparisonOperator comparisonOperator) {
		this.comparisonOperator = comparisonOperator;
	}
	public StatisticsOption getStatisticsOption() {
		return statisticsOption;
	}
	public void setStatisticsOption(StatisticsOption statisticsOption) {
		this.statisticsOption = statisticsOption;
	}
	public double getPeriod() {
		return period;
	}
	public void setPeriod(double period) {
		this.period = period;
	}
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
