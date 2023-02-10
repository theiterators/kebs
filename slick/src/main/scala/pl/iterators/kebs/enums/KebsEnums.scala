package pl.iterators.kebs.enums

import enumeratum.{Enum, EnumEntry}
import enumeratum.values.{ValueEnum, ValueEnumEntry}
import slick.lifted.Isomorphism

trait SlickEnum {
  def enumIsomorphism[E <: EnumEntry](`enum`: Enum[E]): Isomorphism[E, String] = new Isomorphism[E, String](_.entryName, `enum`.withName)
  def uppercaseEnumIsomorphism[E <: EnumEntry](`enum`: Enum[E]): Isomorphism[E, String] =
    new Isomorphism[E, String](_.entryName.toUpperCase, `enum`.withNameUppercaseOnly)
  def lowercaseEnumIsomorphism[E <: EnumEntry](`enum`: Enum[E]): Isomorphism[E, String] =
    new Isomorphism[E, String](_.entryName.toLowerCase, `enum`.withNameLowercaseOnly)

  implicit def enumListColumnType[E <: EnumEntry](implicit iso: Isomorphism[E, String]): Isomorphism[List[E], List[String]] =
    new Isomorphism[List[E], List[String]](_.map(iso.map), _.map(iso.comap))
  implicit def enumSeqColumnType[E <: EnumEntry](implicit iso: Isomorphism[E, String]): Isomorphism[Seq[E], List[String]] =
    new Isomorphism[Seq[E], List[String]](_.map(iso.map).toList, _.map(iso.comap))
}

trait SlickValueEnum {
  def valueEnumIsomorphism[V, E <: ValueEnumEntry[V]](`enum`: ValueEnum[V, E]): Isomorphism[E, V] =
    new Isomorphism[E, V](_.value, `enum`.withValue)
}

trait KebsEnums extends SlickEnum with SlickValueEnum {
  implicit def enumValueColumn[E <: EnumEntry](implicit ev: EnumOf[E]): Isomorphism[E, String] = enumIsomorphism(ev.`enum`)
  implicit def valueEnumColumn[V, E <: ValueEnumEntry[V]](implicit ev: ValueEnumOf[V, E]): Isomorphism[E, V] =
    valueEnumIsomorphism(ev.valueEnum)

  trait Uppercase extends SlickEnum {
    implicit def enumValueColumn[E <: EnumEntry](implicit ev: EnumOf[E]): Isomorphism[E, String] = uppercaseEnumIsomorphism(ev.`enum`)
  }

  trait Lowercase extends SlickEnum {
    implicit def enumValueColumn[E <: EnumEntry](implicit ev: EnumOf[E]): Isomorphism[E, String] = lowercaseEnumIsomorphism(ev.`enum`)
  }
}

object KebsEnums extends KebsEnums
