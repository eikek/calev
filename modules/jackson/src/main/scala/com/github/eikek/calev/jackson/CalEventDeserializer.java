package com.github.eikek.calev.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.github.eikek.calev.CalEvent;

public class CalEventDeserializer extends FromStringDeserializer<CalEvent> {
	protected CalEventDeserializer() {
		super(CalEvent.class);
	}

	@Override
	protected CalEvent _deserialize(String value, DeserializationContext ctxt) {
		return CalEvent.unsafe(value);
	}
}
