package de.unistuttgart.ma.backend.exceptions;

/**
 * Indicates that there was an alert that violated an SLO that did not exist. 
 * @author maumau
 *
 */
public class CouldNotSaveSystemModelException extends Exception {
	public CouldNotSaveSystemModelException(String message) {
		super(message);
	}
}
