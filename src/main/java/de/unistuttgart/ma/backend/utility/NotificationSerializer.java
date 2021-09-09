package de.unistuttgart.ma.backend.utility;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.unistuttgart.ma.impact.Impact;
import de.unistuttgart.ma.impact.Notification;
import de.unistuttgart.ma.impact.Violation;

/**
 * Serialises a {@link Notification} to JSON.
 * 
 * For now, let's skip the violation, because that is already represented by
 * some linked issues.
 * 
 */
public class NotificationSerializer extends StdSerializer<Notification> {

	public NotificationSerializer(Class<Notification> t) {
		super(t);
	}

	@Override
	public void serialize(Notification value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

		jgen.writeStartObject();
		jgen.writeObjectField("impactlocation", value.getTopLevelImpact().getLocation());
		jgen.writeObjectField("violatedrule", value.getRootCause().getViolatedRule());
		serializeImpacts(value.getTopLevelImpact(), jgen);
		// serializeViolation(value.getRootCause(), jgen);
		jgen.writeEndObject();

	}

	protected void serializeViolation(Violation violation, JsonGenerator jgen) throws IOException {
		jgen.writeArrayFieldStart("violations");
		jgen.writeObject(violation);
		jgen.writeEndArray();
	}

	protected void serializeImpacts(Impact impact, JsonGenerator jgen) throws IOException {
		jgen.writeArrayFieldStart("impactpath");

		Impact current = impact;
		while (current != null) {
			jgen.writeObject(current);
			current = current.getCause();
		}
		jgen.writeEndArray();
	}
}
