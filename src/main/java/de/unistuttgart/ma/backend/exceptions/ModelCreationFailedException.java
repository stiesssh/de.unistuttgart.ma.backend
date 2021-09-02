package de.unistuttgart.ma.backend.exceptions;

/**
 * A {@code ModelCreationFailedException } indicates that the creation of a new model failed.
 * 
 * @author maumau
 *
 */
public class ModelCreationFailedException extends Exception {
	public ModelCreationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
