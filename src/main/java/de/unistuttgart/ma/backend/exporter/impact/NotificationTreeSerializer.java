package de.unistuttgart.ma.backend.exporter.impact;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.ma.impact.Notification;

/**
 * 
 * @author maumau
 *
 */
public class NotificationTreeSerializer extends StdSerializer<Notification> {

	public NotificationTreeSerializer(Class<Notification> t) {
		super(t);
	}
	 

	@Override
	public void serialize(Notification value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
				 
        jgen.writeStartObject();        
        jgen.writeObjectField("impacts", value.getTopLevelImpact());
        jgen.writeObjectField("rootcause", value.getRootCause()); 
        jgen.writeEndObject();
		
	}
	
	protected void serializeLocation(EObject location, JsonGenerator jgen) {
		
	}

}
