package com.github.eikek.calev.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.eikek.calev.CalEvent

@SerialVersionUID(1L)
final class CalevModule() extends SimpleModule(PackageVersion.VERSION) {
  addDeserializer(classOf[CalEvent], new CalEventDeserializer)
  addSerializer(classOf[CalEvent], new CalEventSerializer)
}
