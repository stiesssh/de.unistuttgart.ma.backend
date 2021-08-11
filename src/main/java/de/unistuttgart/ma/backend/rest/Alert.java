package de.unistuttgart.ma.backend.rest;

public class Alert {
	public Alert() {}
	
	public Alert(String gropiusProjectId, double actualValue, double actualPeriod, String sloId, String sloName) {
		super();
		this.gropiusProjectId = gropiusProjectId;
		this.actualValue = actualValue;
		this.actualPeriod = actualPeriod;
		this.sloId = sloId;
		this.sloName = sloName;
	}
	double actualValue;
	double actualPeriod;
    String alertName;
    String alertDescription;
    String alertTime;
    String sloId;
    String sloName;
    String triggeringTargetName;
    String gropiusProjectId;
    String gropiusComponentId;
    
	public String getAlertName() {
		return alertName;
	}
	public String getAlertDescription() {
		return alertDescription;
	}
	public String getAlertTime() {
		return alertTime;
	}
	public String getSloId() {
		return sloId;
	}
	public String getSloName() {
		return sloName;
	}
	public String getTriggeringTargetName() {
		return triggeringTargetName;
	}
	public String getGropiusProjectId() {
		return gropiusProjectId;
	}
	public String getGropiusComponentId() {
		return gropiusComponentId;
	}
	public double getActualValue() {
		return actualValue;
	}
	public double getActualPeriod() {
		return actualPeriod;
	}   
}
