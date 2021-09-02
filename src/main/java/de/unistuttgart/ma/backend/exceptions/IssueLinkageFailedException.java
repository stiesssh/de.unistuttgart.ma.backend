package de.unistuttgart.ma.backend.exceptions;

/**
 * A {@code IssueLinkageFailedException} indicates that the linkage of a Gropius issue to another failed.
 *  
 * @author maumau
 *
 */
public class IssueLinkageFailedException extends Exception {
	public IssueLinkageFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
