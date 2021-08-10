package de.unistuttgart.ma.backend.exporter.impact;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.ma.impact.Impact;

/**
 * 
 * @author maumau
 *
 */
public class ImpactListSerializer extends StdSerializer<Impact> {

	public ImpactListSerializer(Class<Impact> t) {
		super(t);
	}
	 

	@Override
	public void serialize(Impact value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
				 
        jgen.writeStartObject();
        jgen.writeStringField("id", value.getId());
        jgen.writeObjectField("location", value.getLocation());
        if (value.getCause() != null) {
        	jgen.writeStringField("cause", value.getCause().getId());
        }
        jgen.writeEndObject();
		
	}
}
