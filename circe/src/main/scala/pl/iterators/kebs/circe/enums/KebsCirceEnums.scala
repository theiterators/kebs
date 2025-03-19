package pl.iterators.kebs.circe.enums

import io.circe._
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

trait KebsCirceEnums {
  @inline protected final def enumNameDeserializationError[E](`enum`: EnumLike[E], name: String): String = {
    val enumNames = `enum`.getNamesToValuesMap.values.mkString(", ")
    s"$name should be one of $enumNames"
  }

  @inline protected final def enumValueDeserializationError[E](`enum`: EnumLike[E], value: Json): String = {
    val enumNames = `enum`.getNamesToValuesMap.values.mkString(", ")
    s"$value should be a string of value $enumNames"
  }

  protected final def enumDecoder[E](`enum`: EnumLike[E], _comap: String => Option[E]): Decoder[E] =
    (c: HCursor) =>
      Decoder.decodeString.emap(str => _comap(str).toRight("")).withErrorMessage(enumValueDeserializationError(`enum`, c.value))(c)

  protected final def enumEncoder[E](`enum`: EnumLike[E], _map: E => String): Encoder[E] =
    (obj: E) => Encoder.encodeString(_map(obj))

  def enumDecoder[E](`enum`: EnumLike[E]): Decoder[E] =
    enumDecoder[E](`enum`, `enum`.withNameInsensitiveOption(_))
  def enumEncoder[E](`enum`: EnumLike[E]): Encoder[E] =
    enumEncoder[E](`enum`, (e: E) => `enum`.getName(e))

  def lowercaseEnumDecoder[E](`enum`: EnumLike[E]): Decoder[E] =
    enumDecoder[E](`enum`, `enum`.withNameLowercaseOnlyOption(_))
  def lowercaseEnumEncoder[E](`enum`: EnumLike[E]): Encoder[E] =
    enumEncoder[E](`enum`, (e: E) => e.toString.toLowerCase)

  def uppercaseEnumDecoder[E](`enum`: EnumLike[E]): Decoder[E] =
    enumDecoder[E](`enum`, `enum`.withNameUppercaseOnlyOption(_))
  def uppercaseEnumEncoder[E](`enum`: EnumLike[E]): Encoder[E] =
    enumEncoder[E](`enum`, (e: E) => e.toString.toUpperCase())

  implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Decoder[E] = enumDecoder(ev)

  implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Encoder[E] = enumEncoder(ev)

  trait KebsCirceEnumsUppercase {
    implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Decoder[E] =
      uppercaseEnumDecoder(ev)

    implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Encoder[E] =
      uppercaseEnumEncoder(ev)
  }

  trait KebsCirceEnumsLowercase {
    implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Decoder[E] =
      lowercaseEnumDecoder(ev)

    implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Encoder[E] =
      lowercaseEnumEncoder(ev)
  }
}

trait KebsCirceValueEnums {
  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnumLikeEntry[V]](
      `enum`: ValueEnumLike[V, E],
      value: Json
  ): String = {
    val enumValues = `enum`.getValuesToEntriesMap.keys.mkString(", ")
    s"$value is not a member of $enumValues"
  }

  def valueEnumDecoder[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit decoder: Decoder[V]): Decoder[E] =
    (c: HCursor) =>
      decoder.emap(obj => `enum`.withValueOption(obj).toRight("")).withErrorMessage(valueEnumDeserializationError(`enum`, c.value))(c)

  def valueEnumEncoder[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit encoder: Encoder[V]): Encoder[E] =
    (obj: E) => encoder(obj.value)

  implicit def valueEnumDecoderImpl[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], decoder: Decoder[V]): Decoder[E] =
    valueEnumDecoder(ev)

  implicit def valueEnumEncoderImpl[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], encoder: Encoder[V]): Encoder[E] =
    valueEnumEncoder(ev)
}
