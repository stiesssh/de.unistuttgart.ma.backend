package de.unistuttgart.ma.backend;

import org.apache.commons.lang3.StringEscapeUtils;
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
	
	@Test
	public void esc() {
		System.out.println(StringEscapeUtils.escapeJson("{\"data\":{\"createIssue\":{\"issue\":{\"id\":\"5ecbf851ae565029\",\"title\":\"test\",\"body\":\"this is body\"}}}}\n"
				+ "")); 
	}

}