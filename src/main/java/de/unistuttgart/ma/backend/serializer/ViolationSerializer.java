package de.unistuttgart.ma.backend.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.ma.impact.Violation;

/**
 * 
 * @author maumau
 *
 */
public class ViolationSerializer extends StdSerializer<Violation> {

	public ViolationSerializer(Class<Violation> t) {
		super(t);
	}
	 

	@Override
	public void serialize(Violation value, JsonGenerator jgen, SerializerProvider provider) throws IOException {				 
        
		jgen.writeStartObject(); 
        jgen.writeStringField("timestamp", value.getStartTime().toString());
		jgen.writeNumberField("value", value.getThreshold());
        jgen.writeEndObject();
	}
}
