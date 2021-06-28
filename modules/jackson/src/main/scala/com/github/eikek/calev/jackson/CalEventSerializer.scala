package com.github.eikek.calev.jackson

import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase
import com.github.eikek.calev.CalEvent

@SerialVersionUID(1L)
class CalEventSerializer extends ToStringSerializerBase(classOf[CalEvent]) {

  override def valueToString(value: Any): String =
    value.asInstanceOf[CalEvent].asString
}
