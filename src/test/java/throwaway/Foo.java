package throwaway;

import org.junit.jupiter.api.Test;

import com.shopify.graphql.support.ID;

import de.unistuttgart.ma.backend.importer.architecture.GropiusApiQueries;

public class Foo {
	@Test
	public void foo() {
		System.out.println(GropiusApiQueries.getLinkIssueMutation(new ID("foo"), new ID("bar")).toString());
	}
}
