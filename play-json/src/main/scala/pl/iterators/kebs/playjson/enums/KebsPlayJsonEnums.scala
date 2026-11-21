package pl.iterators.kebs.playjson.enums

import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}
import play.api.libs.json.{JsError, JsSuccess, Reads, Writes}

trait KebsPlayJsonEnums {
  protected final def enumDecoder[E](`enum`: EnumLike[E], _comap: String => Option[E]): Reads[E] =
    Reads.StringReads.flatMapResult { str =>
      _comap(str) match {
        case Some(e) => JsSuccess(e)
        case None    => JsError(s"$str should be one of ${`enum`.names.mkString(", ")}")
      }
    }

  protected final def enumEncoder[E](`enum`: EnumLike[E], _map: E => String): Writes[E] =
    Writes.StringWrites.contramap(_map)

  def namedEnumDecoder[E](`enum`: EnumLike[E], naming: EnumNaming): Reads[E] = {
    val named = naming.applyTo(`enum`)
    enumDecoder[E](named, (name: String) => naming.lookup(named, name))
  }
  def namedEnumEncoder[E](`enum`: EnumLike[E], naming: EnumNaming): Writes[E] = {
    val named = naming.applyTo(`enum`)
    enumEncoder[E](named, (e: E) => named.getName(e))
  }

  def enumDecoder[E](`enum`: EnumLike[E]): Reads[E]           = namedEnumDecoder(`enum`, EnumNaming.default)
  def enumEncoder[E](`enum`: EnumLike[E]): Writes[E]          = namedEnumEncoder(`enum`, EnumNaming.default)
  def lowercaseEnumDecoder[E](`enum`: EnumLike[E]): Reads[E]  = namedEnumDecoder(`enum`, EnumNaming.lowercase)
  def lowercaseEnumEncoder[E](`enum`: EnumLike[E]): Writes[E] = namedEnumEncoder(`enum`, EnumNaming.lowercase)
  def uppercaseEnumDecoder[E](`enum`: EnumLike[E]): Reads[E]  = namedEnumDecoder(`enum`, EnumNaming.uppercase)
  def uppercaseEnumEncoder[E](`enum`: EnumLike[E]): Writes[E] = namedEnumEncoder(`enum`, EnumNaming.uppercase)

  implicit def enumDecoderImpl[E](implicit ev: EnumLike[E], naming: EnumNaming): Reads[E]  = namedEnumDecoder(ev, naming)
  implicit def enumEncoderImpl[E](implicit ev: EnumLike[E], naming: EnumNaming): Writes[E] = namedEnumEncoder(ev, naming)

  trait KebsPlayJsonEnumsUppercase {
    implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Reads[E]  = uppercaseEnumDecoder(ev)
    implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Writes[E] = uppercaseEnumEncoder(ev)
  }

  trait KebsPlayJsonEnumsLowercase {
    implicit def enumDecoderImpl[E](implicit ev: EnumLike[E]): Reads[E]  = lowercaseEnumDecoder(ev)
    implicit def enumEncoderImpl[E](implicit ev: EnumLike[E]): Writes[E] = lowercaseEnumEncoder(ev)
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
