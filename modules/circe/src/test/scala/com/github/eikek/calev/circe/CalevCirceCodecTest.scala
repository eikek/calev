package com.github.eikek.calev.circe

import com.github.eikek.calev._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import minitest._

object CalevCirceCodecTest extends SimpleTestSuite with CalevCirceCodec {

  def parseJson(str: String): Json =
    parser.parse(str).fold(throw _, identity)

  test("encode/decode literal") {
    val event  = CalEvent.unsafe("Mon *-*-* 5:0/10")
    val parsed = parseJson(s""""${event.asString}"""")
    assertEquals(event.asJson, parsed)
    assertEquals(parsed.as[CalEvent].fold(throw _, identity), event)
  }

  case class Meeting(name: String, event: CalEvent)
  object Meeting {
    implicit val jsonDecoder = deriveDecoder[Meeting]
    implicit val jsonEncoder = deriveEncoder[Meeting]
  }

  test("encode/decode derived") {
    val meeting = Meeting("trash can", CalEvent.unsafe("Mon..Fri *-*-* 14,18:0"))
    val json    = meeting.asJson.noSpaces
    val read    = parseJson(json).as[Meeting].fold(throw _, identity)
    assertEquals(read, meeting)
  }
}
