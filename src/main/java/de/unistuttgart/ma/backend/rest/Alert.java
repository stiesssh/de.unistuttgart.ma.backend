package de.unistuttgart.ma.backend.rest;

import java.time.LocalDateTime;

public class Alert {
	public Alert() {}
	
	public Alert(String gropiusProjectId, double actualValue, double actualPeriod, String sloId, String sloName, String issueId, LocalDateTime alertTime) {
		super();
		this.gropiusProjectId = gropiusProjectId;
		this.actualValue = actualValue;
		this.actualPeriod = actualPeriod;
		this.sloId = sloId;
		this.sloName = sloName;
		this.issueId = issueId;
		this.alertTime = alertTime;
	}
	double actualValue;
	double actualPeriod;
    String alertName;
    String alertDescription;
    LocalDateTime alertTime;
    String sloId;
    String sloName;
    String triggeringTargetName;
    String gropiusProjectId;
    String gropiusComponentId;
    String issueId;
    
	public String getAlertName() {
		return alertName;
	}
	public String getAlertDescription() {
		return alertDescription;
	}
	public LocalDateTime getAlertTime() {
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
	public String getIssueId() {
		return issueId;
	}
}
