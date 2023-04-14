package pl.iterators.kebs.circe

import io.circe.Decoder.Result
import io.circe._
import pl.iterators.kebs.macros.enums.{EnumOf}
import scala.reflect.Enum
import scala.util.Try
import pl.iterators.kebs.enums.ValueEnum
import pl.iterators.kebs.macros.enums.ValueEnumLike
import pl.iterators.kebs.macros.enums.ValueEnumOf
trait CirceEnum {
  @inline protected final def enumNameDeserializationError[E <: Enum](e: EnumOf[E], name: String): String = {
    val enumNames = e.`enum`.values.mkString(", ")
    s"$name should be one of $enumNames"
  }

  @inline protected final def enumValueDeserializationError[E <: Enum](e: EnumOf[E], value: Json): String = {
    val enumNames = e.`enum`.values.mkString(", ")
    s"$value should be a string of value $enumNames"
  }

  protected final def enumDecoder[E <: Enum](e: EnumOf[E], _comap: String => Option[E]): Decoder[E] =
    (c: HCursor) =>
      Decoder.decodeString.emap(str => _comap(str).toRight(""))
      .withErrorMessage(enumValueDeserializationError(e, c.value))(c)

  protected final def enumEncoder[E <: Enum](e: EnumOf[E], _map: E => String): Encoder[E] =
    (obj: E) => Encoder.encodeString(_map(obj))

  def enumDecoder[E <: Enum](e: EnumOf[E]): Decoder[E] =
    enumDecoder[E](e, s => e.`enum`.values.find(_.toString.equalsIgnoreCase(s)))

  def enumEncoder[E <: Enum](e: EnumOf[E]): Encoder[E] =
    enumEncoder[E](e, (e: Enum) => e.toString)

  def lowercaseEnumDecoder[E <: Enum](e: EnumOf[E]): Decoder[E] =
    enumDecoder[E](e, s => e.`enum`.values.find(_.toString.toLowerCase == s))
  def lowercaseEnumEncoder[E <: Enum](e: EnumOf[E]): Encoder[E] =
    enumEncoder[E](e, (e: Enum) => e.toString.toLowerCase)

  def uppercaseEnumDecoder[E <: Enum](e: EnumOf[E]): Decoder[E] =
    enumDecoder[E](e, s => e.`enum`.values.find(_.toString().toUpperCase() == s))
  def uppercaseEnumEncoder[E <: Enum](e: EnumOf[E]): Encoder[E] =
    enumEncoder[E](e, (e: Enum) => e.toString().toUpperCase())
}

trait CirceValueEnum {
  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnum[V] with Enum](e: ValueEnumOf[V, E], value: Json): String = {
    val enumValues = e.`enum`.values.map(_.value.toString()).mkString(", ")
    s"$value is not a member of $enumValues"
  }

  def valueEnumDecoder[V, E <: ValueEnum[V] with Enum](e: ValueEnumOf[V, E])(implicit decoder: Decoder[V]): Decoder[E] =
    (c: HCursor) =>
      decoder.emap(obj => Try(e.`enum`.valueOf(obj)).toOption.toRight("")).withErrorMessage(valueEnumDeserializationError(e, c.value))(c)

  def valueEnumEncoder[V, E <: ValueEnum[V] with Enum](e: ValueEnumOf[V, E])(implicit encoder: Encoder[V]): Encoder[E] =
    (obj: E) => { encoder(obj.value) }
}

trait KebsEnumFormats extends CirceEnum with CirceValueEnum {
  implicit inline given[E <: Enum](using ev: EnumOf[E]): Decoder[E] = enumDecoder(ev)

  implicit inline given[E <: Enum](using ev: EnumOf[E]): Encoder[E] = enumEncoder(ev)

  implicit inline given[V, E <: ValueEnum[V] with Enum](using ev: ValueEnumOf[V, E], decoder: Decoder[V]): Decoder[E] =
    valueEnumDecoder(ev)

  implicit inline given[V, E <: ValueEnum[V] with Enum](using ev: ValueEnumOf[V, E], encoder: Encoder[V]): Encoder[E] =
    valueEnumEncoder(ev)

  trait Uppercase extends CirceEnum {
    implicit inline given[E <: Enum](using ev: EnumOf[E]): Decoder[E] =
      uppercaseEnumDecoder(ev)

    implicit inline given[E <: Enum](using ev: EnumOf[E]): Encoder[E] =
      uppercaseEnumEncoder(ev)
  }

  trait Lowercase extends CirceEnum {
    implicit inline given[E <: Enum](using ev: EnumOf[E]): Decoder[E] =
      lowercaseEnumDecoder(ev)

    implicit inline given[E <: Enum](using ev: EnumOf[E]): Encoder[E] =
      lowercaseEnumEncoder(ev)
  }
}

object KebsEnumFormats extends KebsEnumFormats
