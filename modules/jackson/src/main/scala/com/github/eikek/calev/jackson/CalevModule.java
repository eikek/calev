package com.github.eikek.calev.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.eikek.calev.CalEvent;

public final class CalevModule extends SimpleModule {
	private static final long serialVersionUID = 1L;

	public CalevModule() {
		super(PackageVersion.VERSION);

		addDeserializer(CalEvent.class, new CalEventDeserializer());
		addSerializer(CalEvent.class, new CalEventSerializer());
	}

}
