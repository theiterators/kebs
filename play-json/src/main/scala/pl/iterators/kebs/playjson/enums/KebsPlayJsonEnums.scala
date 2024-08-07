package pl.iterators.kebs.playjson.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import play.api.libs.json.{JsError, JsSuccess, Reads, Writes}

trait KebsPlayJsonEnums {
  protected final def enumDecoder[E](`enum`: EnumLike[E], _comap: String => Option[E]): Reads[E] =
    Reads.StringReads.flatMapResult { str =>
      _comap(str) match {
        case Some(e) => JsSuccess(e)
        case None    => JsError(s"$str should be one of ${`enum`.getNamesToValuesMap.values.mkString(", ")}")
      }
    }

  protected final def enumEncoder[E](`enum`: EnumLike[E], _map: E => String): Writes[E] =
    Writes.StringWrites.contramap(_map)

  def enumDecoder[E](`enum`: EnumLike[E]): Reads[E] =
    enumDecoder[E](`enum`, `enum`.withNameInsensitiveOption(_))
  def enumEncoder[E](`enum`: EnumLike[E]): Writes[E] =
    enumEncoder[E](`enum`, (e: E) => e.toString)

  def lowercaseEnumDecoder[E](`enum`: EnumLike[E]): Reads[E] =
    enumDecoder[E](`enum`, `enum`.withNameLowercaseOnlyOption(_))
  def lowercaseEnumEncoder[E](`enum`: EnumLike[E]): Writes[E] =
    enumEncoder[E](`enum`, (e: E) => e.toString.toLowerCase)

  def uppercaseEnumDecoder[E](`enum`: EnumLike[E]): Reads[E] =
    enumDecoder[E](`enum`, `enum`.withNameUppercaseOnlyOption(_))
  def uppercaseEnumEncoder[E](`enum`: EnumLike[E]): Writes[E] =
    enumEncoder[E](`enum`, (e: E) => e.toString.toUpperCase())

  implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Reads[E] = enumDecoder(ev)

  implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Writes[E] = enumEncoder(ev)

  trait KebsCirceEnumsUppercase {
    implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Reads[E] =
      uppercaseEnumDecoder(ev)

    implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Writes[E] =
      uppercaseEnumEncoder(ev)
  }

  trait KebsCirceEnumsLowercase {
    implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Reads[E] =
      lowercaseEnumDecoder(ev)

    implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Writes[E] =
      lowercaseEnumEncoder(ev)
  }
}

trait KebsPlayJsonValueEnums {
  def valueEnumDecoder[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit decoder: Reads[V]): Reads[E] =
    Reads.of[V].flatMapResult { v =>
      `enum`.withValueOption(v) match {
        case Some(e) => JsSuccess(e)
        case None    => JsError(s"$v is not a member of ${`enum`.getValuesToEntriesMap.keys.mkString(", ")}")
      }
    }

  def valueEnumEncoder[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit encoder: Writes[V]): Writes[E] =
    Writes.of[V].contramap(_.value)

  implicit def valueEnumDecoderImpl[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], decoder: Reads[V]): Reads[E] =
    valueEnumDecoder(ev)

  implicit def valueEnumEncoderImpl[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], encoder: Writes[V]): Writes[E] =
    valueEnumEncoder(ev)
}
