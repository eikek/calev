package com.github.eikek.calev.jackson

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.eikek.calev._
import com.github.eikek.calev.jackson.CalevJacksonSerializerTest.Meeting
import munit._

object CalevJacksonSerializerTest {
  case class Meeting(name: String, event: CalEvent)
}

class CalevJacksonSerializerTest extends FunSuite {

  val jackson = JsonMapper
    .builder()
    .addModule(new CalevModule())
    .addModule(DefaultScalaModule)
    .build()

  test("serialize/deserialize literal") {
    val eventStr = "Mon *-*-* 05:00/10:00"
    val event    = CalEvent.unsafe(eventStr)

    val eventSerialized = jackson.writeValueAsString(event)
    assertEquals(eventSerialized, "\"" + eventStr + "\"")

    val eventDeserialized =
      jackson.readValue(eventSerialized, new TypeReference[CalEvent] {})
    assertEquals(event, eventDeserialized)
  }

  test("encode/decode derived") {
    val meeting = Meeting("trash can", CalEvent.unsafe("Mon..Fri *-*-* 14,18:0"))
    val json    = jackson.writeValueAsString(meeting)
    val read    = jackson.readValue(json, new TypeReference[Meeting] {})
    assertEquals(read, meeting)
  }

}
