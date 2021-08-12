package de.unistuttgart.ma.backend.serializer;

import java.io.IOException;

import org.eclipse.bpmn2.Task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * 
 * @author maumau
 *
 */
public class TaskSerializer extends StdSerializer<Task> {

	public TaskSerializer(Class<Task> t) {
		super(t);
	}
	 

	@Override
	public void serialize(Task value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
        jgen.writeStringField("name", value.getName());
        jgen.writeStringField("id", value.getId().toString());
        jgen.writeEndObject();	
	}

}
