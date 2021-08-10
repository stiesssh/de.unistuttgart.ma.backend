package de.unistuttgart.ma.backend.exporter.impact;

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
        jgen.writeObjectField("slorule", value.getViolatedRule());
       
        serializeActual(value, jgen);
        
        jgen.writeEndObject();
		
	}
	
	protected void serializeActual(Violation value, JsonGenerator jgen) throws IOException {
		jgen.writeFieldName("actual");
		jgen.writeStartObject();
		jgen.writeNumberField("threshold", value.getThreshold());
		jgen.writeNumberField("period", value.getPeriod());
		jgen.writeEndObject();
		
	}

}
