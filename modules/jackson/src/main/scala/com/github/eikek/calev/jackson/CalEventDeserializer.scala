package com.github.eikek.calev.jackson

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer
import com.github.eikek.calev.CalEvent

class CalEventDeserializer extends FromStringDeserializer[CalEvent](classOf[CalEvent]) {
  override protected def _deserialize(
      value: String,
      ctxt: DeserializationContext
  ): CalEvent =
    CalEvent.unsafe(value)
}
