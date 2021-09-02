package de.unistuttgart.ma.backend.exceptions;

/**
 * A {@code MappingFailedException} indicates that the {@link DataMapper} failed to map.
 * 
 * TODO actually use this.
 * 
 * @author maumau
 *
 */
public class MappingFailedException extends Exception {
	public MappingFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
