package pl.iterators.kebs.circe.internal

import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.circe.KebsCirceMacros
import pl.iterators.kebs.core.json.JsonConfig

import scala.language.experimental.macros

// called by the code the entry points generate
object CirceLocalCodecs {
  def encoder[T](config: JsonConfig): Encoder.AsObject[T] = macro KebsCirceMacros.oneEncoder[T]
  def decoder[T](config: JsonConfig): Decoder[T] = macro KebsCirceMacros.oneDecoder[T]
}
