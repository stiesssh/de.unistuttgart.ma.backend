package de.unistuttgart.ma.backend;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;

public class Playground {
	@Test
	public void foo() {
		//URI uri = URI.createFileURI("fiiii/foo.saga");
		URI uri = URI.createPlatformResourceURI("fiiii/foo.saga", false);
		
		System.err.println(uri.toString());
		System.err.println(uri.lastSegment());
		System.err.println(uri.path());
		System.err.println(uri.toPlatformString(false));
		
	}

}