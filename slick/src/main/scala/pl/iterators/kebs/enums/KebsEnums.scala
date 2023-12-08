package pl.iterators.kebs.enums

import slick.lifted.Isomorphism

trait SlickEnum {
  def enumIsomorphism[E](`enum`: EnumLike[E]): Isomorphism[E, String] = new Isomorphism[E, String](_.toString, `enum`.withName)
  def uppercaseEnumIsomorphism[E](`enum`: EnumLike[E]): Isomorphism[E, String] =
    new Isomorphism[E, String](_.toString.toUpperCase, `enum`.withNameUppercaseOnly)
  def lowercaseEnumIsomorphism[E](`enum`: EnumLike[E]): Isomorphism[E, String] =
    new Isomorphism[E, String](_.toString.toLowerCase, `enum`.withNameLowercaseOnly)

  implicit def enumListColumnType[E](implicit iso: Isomorphism[E, String]): Isomorphism[List[E], List[String]] =
    new Isomorphism[List[E], List[String]](_.map(iso.map), _.map(iso.comap))
  implicit def enumSeqColumnType[E](implicit iso: Isomorphism[E, String]): Isomorphism[Seq[E], List[String]] =
    new Isomorphism[Seq[E], List[String]](_.map(iso.map).toList, _.map(iso.comap))
}

trait SlickValueEnum {
  def valueEnumIsomorphism[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E]): Isomorphism[E, V] =
    new Isomorphism[E, V](_.value, `enum`.withValue)
}

trait KebsEnums extends SlickEnum with SlickValueEnum {
  implicit def enumValueColumn[E](implicit ev: EnumLike[E]): Isomorphism[E, String] = enumIsomorphism(ev)
  implicit def valueEnumColumn[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E]): Isomorphism[E, V] =
    valueEnumIsomorphism(ev)

  trait Uppercase extends SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E]): Isomorphism[E, String] = uppercaseEnumIsomorphism(ev)
  }

  trait Lowercase extends SlickEnum {
    implicit def enumValueColumn[E](implicit ev: EnumLike[E]): Isomorphism[E, String] = lowercaseEnumIsomorphism(ev)
  }
}

object KebsEnums extends KebsEnums
