package pl.iterators.kebs.circe

import io.circe._
import scala.reflect.Enum
import scala.util.Try

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait CirceEnum {
  @inline protected final def enumNameDeserializationError[E <: Enum](e: EnumLike[E], name: String): String = {
    val enumNames = e.values.mkString(", ")
    s"$name should be one of $enumNames"
  }

  @inline protected final def enumValueDeserializationError[E <: Enum](e: EnumLike[E], value: Json): String = {
    val enumNames = e.values.mkString(", ")
    s"$value should be a string of value $enumNames"
  }

  protected final def enumDecoder[E <: Enum](e: EnumLike[E], _comap: String => Option[E]): Decoder[E] =
    (c: HCursor) =>
      Decoder.decodeString
        .emap(str => _comap(str).toRight(""))
        .withErrorMessage(enumValueDeserializationError(e, c.value))(c)

  protected final def enumEncoder[E <: Enum](e: EnumLike[E], _map: E => String): Encoder[E] =
    (obj: E) => Encoder.encodeString(_map(obj))

  def enumDecoder[E <: Enum](e: EnumLike[E]): Decoder[E] =
    enumDecoder[E](e, s => e.values.find(_.toString.equalsIgnoreCase(s)))

  def enumEncoder[E <: Enum](e: EnumLike[E]): Encoder[E] =
    enumEncoder[E](e, (e: Enum) => e.toString)

  def lowercaseEnumDecoder[E <: Enum](e: EnumLike[E]): Decoder[E] =
    enumDecoder[E](e, s => e.values.find(_.toString.toLowerCase == s))
  def lowercaseEnumEncoder[E <: Enum](e: EnumLike[E]): Encoder[E] =
    enumEncoder[E](e, (e: Enum) => e.toString.toLowerCase)

  def uppercaseEnumDecoder[E <: Enum](e: EnumLike[E]): Decoder[E] =
    enumDecoder[E](e, s => e.values.find(_.toString().toUpperCase() == s))
  def uppercaseEnumEncoder[E <: Enum](e: EnumLike[E]): Encoder[E] =
    enumEncoder[E](e, (e: Enum) => e.toString().toUpperCase())
}

trait CirceValueEnum {
  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnumLikeEntry[V]](e: ValueEnumLike[V, E], value: Json): String = {
    val enumValues = e.values.map(_.value.toString()).mkString(", ")
    s"$value is not a member of $enumValues"
  }

  def valueEnumDecoder[V, E <: ValueEnumLikeEntry[V]](e: ValueEnumLike[V, E])(implicit decoder: Decoder[V]): Decoder[E] =
    (c: HCursor) =>
      decoder.emap(obj => Try(e.valueOf(obj)).toOption.toRight("")).withErrorMessage(valueEnumDeserializationError(e, c.value))(c)

  def valueEnumEncoder[V, E <: ValueEnumLikeEntry[V]](e: ValueEnumLike[V, E])(implicit encoder: Encoder[V]): Encoder[E] =
    (obj: E) => { encoder(obj.value) }
}

trait KebsEnumFormats extends CirceEnum with CirceValueEnum {
  inline implicit def decoderFromEnumLike[E <: Enum](using ev: EnumLike[E]): Decoder[E] = enumDecoder(ev)

  inline implicit def encoderFromEnumLike[E <: Enum](using ev: EnumLike[E]): Encoder[E] = enumEncoder(ev)

  inline implicit def decoderFromValueEnumLike[V, E <: ValueEnumLikeEntry[V]](using
      ev: ValueEnumLike[V, E],
      decoder: Decoder[V]
  ): Decoder[E] =
    valueEnumDecoder(ev)

  inline implicit def encoderFromValueEnumLike[V, E <: ValueEnumLikeEntry[V]](using
      ev: ValueEnumLike[V, E],
      encoder: Encoder[V]
  ): Encoder[E] =
    valueEnumEncoder(ev)

  trait Uppercase extends CirceEnum {
    inline implicit def decoderFromEnumLike[E <: Enum](using ev: EnumLike[E]): Decoder[E] =
      uppercaseEnumDecoder(ev)

    inline implicit def encoderFromEnumLike[E <: Enum](using ev: EnumLike[E]): Encoder[E] =
      uppercaseEnumEncoder(ev)
  }

  trait Lowercase extends CirceEnum {
    inline implicit def decoderFromEnumLike[E <: Enum](using ev: EnumLike[E]): Decoder[E] =
      lowercaseEnumDecoder(ev)

    inline implicit def encoderFromEnumLike[E <: Enum](using ev: EnumLike[E]): Encoder[E] =
      lowercaseEnumEncoder(ev)
  }
}

object KebsEnumFormats extends KebsEnumFormats
