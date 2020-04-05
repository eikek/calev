package com.github.eikek.calev.circe

import io.circe._
import com.github.eikek.calev._

trait CalevCirceCodec {

  implicit val caleventDecoder: Decoder[CalEvent] =
    Decoder.decodeString.emap(CalEvent.parse)

  implicit val caleventEncoder: Encoder[CalEvent] =
    Encoder.encodeString.contramap(_.asString)

}

object CalevCirceCodec extends CalevCirceCodec
