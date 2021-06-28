package com.github.eikek.calev.jackson

import java.io.IOException

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase
import com.github.eikek.calev.CalEvent

@SerialVersionUID(1L)
class CalEventSerializer() extends ToStringSerializerBase(classOf[CalEvent]) {
  @throws[IOException]
  override def serializeWithType(
      value: Any,
      g: JsonGenerator,
      provider: SerializerProvider,
      typeSer: TypeSerializer
  ): Unit = {
    val typeIdDef = typeSer.writeTypePrefix(
      g,
      typeSer.typeId(value, classOf[CalEvent], JsonToken.VALUE_STRING)
    )
    serialize(value, g, provider)
    typeSer.writeTypeSuffix(g, typeIdDef)
    ()
  }
  override def valueToString(value: Any): String =
    value.asInstanceOf[CalEvent].asString
}
