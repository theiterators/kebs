package pl.iterators.kebs.circe

import io.circe.Decoder.Result
import io.circe._
import scala.reflect.Enum
import scala.util.Try

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

import reflect.Selectable.reflectiveSelectable

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
      Decoder.decodeString.emap(str => _comap(str).toRight(""))
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
  implicit inline def kebsEnumDecoder[E <: Enum](using ev: EnumLike[E]): Decoder[E] = enumDecoder(ev)

  implicit inline def kebsEnumEncoder[E <: Enum](using ev: EnumLike[E]): Encoder[E] = enumEncoder(ev)

  implicit inline def kebsValueEnumDecoder[V, E <: ValueEnumLikeEntry[V]](using ev: ValueEnumLike[V, E], decoder: Decoder[V]): Decoder[E] =
    valueEnumDecoder(ev)

  implicit inline def kebsValueEnumEncoder[V, E <: ValueEnumLikeEntry[V]](using ev: ValueEnumLike[V, E], encoder: Encoder[V]): Encoder[E] =
    valueEnumEncoder(ev)

  trait Uppercase extends CirceEnum {
    implicit inline def kebsUppercaseEnumDecoder[E <: Enum](using ev: EnumLike[E]): Decoder[E] =
      uppercaseEnumDecoder(ev)

    implicit inline def kebsUppercaseEnumEncoder[E <: Enum](using ev: EnumLike[E]): Encoder[E] =
      uppercaseEnumEncoder(ev)
  }

  trait Lowercase extends CirceEnum {
    implicit inline def kebsLowercaseEnumDecoder[E <: Enum](using ev: EnumLike[E]): Decoder[E] =
      lowercaseEnumDecoder(ev)

    implicit inline def kebsLowercaseEnumEncoder[E <: Enum](using ev: EnumLike[E]): Encoder[E] =
      lowercaseEnumEncoder(ev)
  }
}

object KebsEnumFormats extends KebsEnumFormats
