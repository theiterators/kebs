package pl.iterators.kebs.enums

import enumeratum.{Enum, EnumEntry}
import enumeratum.values.{ValueEnum, ValueEnumEntry}
import slick.lifted.Isomorphism

trait SlickEnum {
  def enumIsomorphism[E <: EnumEntry](enum: Enum[E]): Isomorphism[E, String] = new Isomorphism[E, String](_.entryName, enum.withName(_))
  def uppercaseEnumIsomorphism[E <: EnumEntry](enum: Enum[E]): Isomorphism[E, String] =
    new Isomorphism[E, String](_.entryName.toUpperCase, enum.withNameUppercaseOnly(_))
  def lowercaseEnumIsomorphism[E <: EnumEntry](enum: Enum[E]): Isomorphism[E, String] =
    new Isomorphism[E, String](_.entryName.toLowerCase, enum.withNameLowercaseOnly(_))

  implicit def enumListColumnType[E <: EnumEntry](implicit iso: Isomorphism[E, String]): Isomorphism[List[E], List[String]] =
    new Isomorphism[List[E], List[String]](_.map(iso.map), _.map(iso.comap))
  implicit def enumSeqColumnType[E <: EnumEntry](implicit iso: Isomorphism[E, String]): Isomorphism[Seq[E], List[String]] =
    new Isomorphism[Seq[E], List[String]](_.map(iso.map).toList, _.map(iso.comap))
}

trait SlickValueEnum {
  def valueEnumIsomorphism[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E]): Isomorphism[E, V] =
    new Isomorphism[E, V](_.value, enum.withValue(_))
}

trait KebsEnums extends SlickEnum with SlickValueEnum {
  import macros.KebsEnumMacros

  implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumn[E]
  implicit def valueEnumColumn[V, E <: ValueEnumEntry[V]]: Isomorphism[E, V] = macro KebsEnumMacros.materializeValueEnumColumn[E, V]

  trait Uppercase extends SlickEnum {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumnWithNameUppercase[E]
  }

  trait Lowercase extends SlickEnum {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumnWithNameLowercase[E]
  }
}

object KebsEnums extends KebsEnums
