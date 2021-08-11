package de.unistuttgart.ma.backend.exceptions;

/**
 * Indicates that there was an alert that violated an SLO that did not exist. 
 * @author maumau
 *
 */
public class IssueLinkageFailedException extends Exception {
	public IssueLinkageFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
