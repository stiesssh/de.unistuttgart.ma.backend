package de.unistuttgart.ma.backend.exceptions;

/**
 * Indicates that there was an alert that violated an SLO that did not exist. 
 * @author maumau
 *
 */
public class MissingSystemModelException extends Exception {
	public MissingSystemModelException(String message) {
		super(message);
	}
}
