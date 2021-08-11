package throwaway;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.Notification;

public class FooTestSchema {

	/**
	 * look at this, this is how you generate json schema from pojo. suck though. 
	 */
	@Test
	public void generateSchema() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
			com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaGen.generateSchema(Impact.class);
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Test, that schema and json match.
	 * 
	 * @throws IOException
	 */
	@Test
	public void minimal_impact_test() throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();

		InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./json/notification.schema.json");
		InputStream jsonStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./json/notification.json");

		JsonNode json = objectMapper.readTree(jsonStream);
		JsonSchema schema = schemaFactory.getSchema(schemaStream);
		Set<ValidationMessage> validationResult = schema.validate(json);

		// print validation errors
		if (!validationResult.isEmpty()) {
			validationResult.forEach(vm -> System.out.println(vm.getMessage()));
			fail();
		}
	}
	
	
	/**
	 * Figure out how json schema validation works.
	 * 
	 * @throws IOException
	 */
	@Test
	public void minimal_test() throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();

		InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./json/json.schema.json");
		InputStream jsonStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./json/json.json");

		JsonNode json = objectMapper.readTree(jsonStream);
		JsonSchema schema = schemaFactory.getSchema(schemaStream);
		Set<ValidationMessage> validationResult = schema.validate(json);

		// print validation errors
		if (!validationResult.isEmpty()) {
			validationResult.forEach(vm -> System.out.println(vm.getMessage()));
			fail();
		}
	}
}
