package de.unistuttgart.ma.backend.exceptions;

/**
 * A {@code IssueCreationFailedException} indicates that the creation of a Gropius issue failed.  
 * 
 * @author maumau
 *
 */
public class IssueCreationFailedException extends Exception {
	public IssueCreationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
