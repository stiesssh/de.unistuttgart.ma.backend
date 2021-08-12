package de.unistuttgart.ma.backend.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.gropius.slo.SloRule;

/**
 * Serializes a SLO rule. 
 * Focus on rule content, locations already represented by impact. 
 *  
 * @author maumau
 *
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
		jgen.writeNumberField("threshold", value.getThreshold());
		jgen.writeNumberField("period", value.getPeriod());
		// TODO : others : statistics and stuff.
        jgen.writeEndObject();
	}
}
