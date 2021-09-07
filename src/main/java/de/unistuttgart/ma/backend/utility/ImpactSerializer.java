package de.unistuttgart.ma.backend.utility;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.ma.impact.Impact;

/**
 * 
 * @author maumau
 *
 */
public class ImpactSerializer extends StdSerializer<Impact> {

	public ImpactSerializer(Class<Impact> t) {
		super(t);
	}
	 

	@Override
	public void serialize(Impact value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
				 
        jgen.writeStartObject();
        jgen.writeStringField("id", value.getLocationId());
        jgen.writeStringField("name", value.getLocationName());
        jgen.writeStringField("type", value.getLocationType());

        jgen.writeFieldName("container");
        jgen.writeStartObject();
        jgen.writeStringField("id", value.getLocationContainerId());
        jgen.writeStringField("name", value.getLocationContainerName());
        jgen.writeStringField("type", value.getLocationContainerType());
        jgen.writeEndObject();
        
        if (value.getCause() != null) {
        	jgen.writeStringField("cause", value.getCause().getLocationId());
        }
        jgen.writeEndObject();
		
	}
}
