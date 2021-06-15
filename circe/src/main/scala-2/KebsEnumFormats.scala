package pl.iterators.kebs.circe

import enumeratum.values.{ValueEnum, ValueEnumEntry}
import enumeratum.{Enum, EnumEntry}
import io.circe.Decoder.Result
import io.circe._
import pl.iterators.kebs.macros.enums.{EnumOf, ValueEnumOf}

trait CirceEnum {
  @inline protected final def enumNameDeserializationError[E <: EnumEntry](enum: Enum[E], name: String): String = {
    val enumNames = enum.namesToValuesMap.values.mkString(", ")
    s"$name should be one of $enumNames"
  }

  @inline protected final def enumValueDeserializationError[E <: EnumEntry](enum: Enum[E], value: Json): String = {
    val enumNames = enum.namesToValuesMap.values.mkString(", ")
    s"$value should be a string of value $enumNames"
  }

  protected final def enumDecoder[E <: EnumEntry](enum: Enum[E], _comap: String => Option[E]): Decoder[E] =
    (c: HCursor) =>
      Decoder.decodeString.emap(str => _comap(str).toRight("")).withErrorMessage(enumValueDeserializationError(enum, c.value))(c)

  protected final def enumEncoder[E <: EnumEntry](enum: Enum[E], _map: E => String): Encoder[E] =
    (obj: E) => Encoder.encodeString(_map(obj))

  def enumDecoder[E <: EnumEntry](enum: Enum[E]): Decoder[E] =
    enumDecoder[E](enum, enum.withNameInsensitiveOption(_))
  def enumEncoder[E <: EnumEntry](enum: Enum[E]): Encoder[E] =
    enumEncoder[E](enum, (e: EnumEntry) => e.entryName)

  def lowercaseEnumDecoder[E <: EnumEntry](enum: Enum[E]): Decoder[E] =
    enumDecoder[E](enum, enum.withNameLowercaseOnlyOption(_))
  def lowercaseEnumEncoder[E <: EnumEntry](enum: Enum[E]): Encoder[E] =
    enumEncoder[E](enum, (e: EnumEntry) => e.entryName.toLowerCase)

  def uppercaseEnumDecoder[E <: EnumEntry](enum: Enum[E]): Decoder[E] =
    enumDecoder[E](enum, enum.withNameUppercaseOnlyOption(_))
  def uppercaseEnumEncoder[E <: EnumEntry](enum: Enum[E]): Encoder[E] =
    enumEncoder[E](enum, (e: EnumEntry) => e.entryName.toUpperCase())
}

trait CirceValueEnum {
  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E], value: Json): String = {
    val enumValues = enum.valuesToEntriesMap.keys.mkString(", ")
    s"$value is not a member of $enumValues"
  }

  def valueEnumDecoder[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E])(implicit decoder: Decoder[V]): Decoder[E] =
    (c: HCursor) =>
      decoder.emap(obj => enum.withValueOpt(obj).toRight("")).withErrorMessage(valueEnumDeserializationError(enum, c.value))(c)

  def valueEnumEncoder[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E])(implicit encoder: Encoder[V]): Encoder[E] =
    (obj: E) => encoder(obj.value)
}

trait KebsEnumFormats extends CirceEnum with CirceValueEnum {
  implicit def enumDecoder[E <: EnumEntry](implicit ev: EnumOf[E]): Decoder[E] = enumDecoder(ev.enum)

  implicit def enumEncoder[E <: EnumEntry](implicit ev: EnumOf[E]): Encoder[E] = enumEncoder(ev.enum)

  implicit def valueEnumDecoder[V, E <: ValueEnumEntry[V]](implicit ev: ValueEnumOf[V, E], decoder: Decoder[V]): Decoder[E] =
    valueEnumDecoder(ev.valueEnum)

  implicit def valueEnumEncoder[V, E <: ValueEnumEntry[V]](implicit ev: ValueEnumOf[V, E], encoder: Encoder[V]): Encoder[E] =
    valueEnumEncoder(ev.valueEnum)

  trait Uppercase extends CirceEnum {
    implicit def enumDecoder[E <: EnumEntry](implicit ev: EnumOf[E]): Decoder[E] =
      uppercaseEnumDecoder(ev.enum)

    implicit def enumEncoder[E <: EnumEntry](implicit ev: EnumOf[E]): Encoder[E] =
      uppercaseEnumEncoder(ev.enum)
  }

  trait Lowercase extends CirceEnum {
    implicit def enumDecoder[E <: EnumEntry](implicit ev: EnumOf[E]): Decoder[E] =
      lowercaseEnumDecoder(ev.enum)

    implicit def enumEncoder[E <: EnumEntry](implicit ev: EnumOf[E]): Encoder[E] =
      lowercaseEnumEncoder(ev.enum)
  }
}

object KebsEnumFormats extends KebsEnumFormats
