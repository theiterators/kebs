package pl.iterators.kebs.enums

import enumeratum.EnumEntry
import slick.lifted.Isomorphism

trait KebsEnums {
  import macros.KebsEnumMacros

  implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumn[E]

  trait Uppercase {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumnWithNameUppercase[E]
  }

  trait Lowercase {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, String] = macro KebsEnumMacros.materializeEnumColumnWithNameLowercase[E]
  }

  trait AsInt {
    implicit def enumValueColumn[E <: EnumEntry]: Isomorphism[E, Int] = macro KebsEnumMacros.materializeEnumColumnWithIndex[E]
  }

}

object KebsEnums extends KebsEnums
