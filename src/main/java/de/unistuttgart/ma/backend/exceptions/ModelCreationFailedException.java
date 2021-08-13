package de.unistuttgart.ma.backend.exceptions;

/**
 * Indicates that there was an alert that violated an SLO that did not exist. 
 * @author maumau
 *
 */
public class ModelCreationFailedException extends Exception {
	public ModelCreationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
