package de.unistuttgart.ma.backend.utility;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.impact.Violation;

/**
 * Serialises a {@link SloRule} to JSON.
 * 
 * Serialises to name and id only, because the details can already be found else
 * where (e.g. a linked issue...)
 */
public class SloRuleSerializer extends StdSerializer<SloRule> {

	public SloRuleSerializer(Class<SloRule> t) {
		super(t);
	}

	@Override
	public void serialize(SloRule value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

		jgen.writeStartObject();
		jgen.writeStringField("name", value.getName());
		jgen.writeStringField("id", value.getId());
		// jgen.writeNumberField("threshold", value.getThreshold());
		// jgen.writeNumberField("period", value.getPeriod());
		jgen.writeEndObject();
	}
}