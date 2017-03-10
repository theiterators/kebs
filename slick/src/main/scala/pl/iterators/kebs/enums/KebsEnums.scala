package pl.iterators.kebs.enums

import enumeratum.EnumEntry
import enumeratum.values.ValueEnumEntry
import slick.lifted.Isomorphism

trait KebsEnums {
  import macros.KebsEnumMacros

  implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumn[E]
  implicit def valueEnumColumn[V, E <: ValueEnumEntry[V]]: Isomorphism[E, V] = macro KebsEnumMacros.materializeValueEnumColumn[E, V]

  trait Uppercase {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumnWithNameUppercase[E]
  }

  trait Lowercase {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumnWithNameLowercase[E]
  }
}

object KebsEnums extends KebsEnums
