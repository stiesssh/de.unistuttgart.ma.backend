package de.unistuttgart.ma.backend.exceptions;

/**
 * Indicates that there was an alert that violated an SLO that did not exist. 
 * @author maumau
 *
 */
public class IssueCreationFailedException extends Exception {
	public IssueCreationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
