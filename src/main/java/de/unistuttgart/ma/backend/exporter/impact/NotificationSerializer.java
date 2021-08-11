package de.unistuttgart.ma.backend.exporter.impact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.bpmn2.Task;
import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.gropius.api.ComponentInterface;
import de.unistuttgart.gropius.slo.SloRule;
import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;
import de.unistuttgart.ma.saga.SagaStep;

/**
 * 
 * @author maumau
 *
 */
public class NotificationSerializer extends StdSerializer<Notification> {

	
	public NotificationSerializer(Class<Notification> t) {
		super(t);
	}
	 

	@Override
	public void serialize(Notification value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
				 
        jgen.writeStartObject();        
        serializeImpacts(value.getTopLevelImpact(), jgen);
        jgen.writeObjectField("rootcause", value.getRootCause()); 
        jgen.writeEndObject();
		
	}
	
	protected void serializeImpacts(Impact impact, JsonGenerator jgen) throws IOException {
		
		
		jgen.writeArrayFieldStart("impacts");
		
		Impact current = impact;
		while (current != null) {
			jgen.writeObject(current);
			current = current.getCause();
		}
		jgen.writeEndArray();
	}
}
