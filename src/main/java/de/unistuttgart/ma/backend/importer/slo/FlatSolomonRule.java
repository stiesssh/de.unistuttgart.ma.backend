package de.unistuttgart.ma.backend.importer.slo;

public class FlatSolomonRule {  
	private String id;
	private String name;
	private String description;
	private String deploymentEnvironment;
	private String targetId;
	private String gropiusProjectId;
	private String gropiusComponentId;
	private String preset;
	private String metricOption;
	private String comparisonOperator;
	private String statistic;
	private double period;
	private double threshold;
	
	public FlatSolomonRule(String id, String name, String description, String deploymentEnvironment, String targetId,
			String gropiusProjectId, String gropiusComponentId, String preset, String metricOption,
			String comparisonOperator, String statistic, double period, double threshold) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.deploymentEnvironment = deploymentEnvironment;
		this.targetId = targetId;
		this.gropiusProjectId = gropiusProjectId;
		this.gropiusComponentId = gropiusComponentId;
		this.preset = preset;
		this.metricOption = metricOption;
		this.comparisonOperator = comparisonOperator;
		this.statistic = statistic;
		this.period = period;
		this.threshold = threshold;
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getDeploymentEnvironment() {
		return deploymentEnvironment;
	}
	public String getTargetId() {
		return targetId;
	}
	public String getGropiusProjectId() {
		return gropiusProjectId;
	}
	public String getGropiusComponentId() {
		return gropiusComponentId;
	}
	public String getPreset() {
		return preset;
	}
	public String getMetricOption() {
		return metricOption;
	}
	public String getComparisonOperator() {
		return comparisonOperator;
	}
	public String getStatistic() {
		return statistic;
	}
	public double getPeriod() {
		return period;
	}
	public double getThreshold() {
		return threshold;
	}
	
	
}
