package com.github.eikek.calev.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import com.github.eikek.calev.CalEvent;

public class CalEventSerializer extends ToStringSerializerBase {
	private static final long serialVersionUID = 1L;

	public CalEventSerializer() {
		super(CalEvent.class);
	}

	@Override
	public void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
		WritableTypeId typeIdDef = typeSer.writeTypePrefix(g,
				typeSer.typeId(value, CalEvent.class, JsonToken.VALUE_STRING));
		serialize(value, g, provider);
		typeSer.writeTypeSuffix(g, typeIdDef);
	}

	@Override
	public String valueToString(Object value) {
		return ((CalEvent) value).asString();
	}
}
