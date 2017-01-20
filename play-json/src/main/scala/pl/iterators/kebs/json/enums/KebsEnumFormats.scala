package pl.iterators.kebs.json.enums

import enumeratum.EnumEntry
import pl.iterators.kebs.json.KebsPlay.InvariantDummy
import play.api.libs.json._

trait KebsEnumFormats {
  import macros.KebsPlayEnumMacros

  implicit def enumReads[E <: EnumEntry]: Reads[E] = macro KebsPlayEnumMacros.materializeEnumReads[E]
  implicit def enumWrites[E <: EnumEntry: InvariantDummy]: Writes[E] = macro KebsPlayEnumMacros.materializeEnumWrites[E]

  trait Uppercase {
    implicit def enumReads[E <: EnumEntry]: Reads[E] = macro KebsPlayEnumMacros.materializeEnumUppercaseReads[E]
    implicit def enumFormat[E <: EnumEntry: InvariantDummy]: Writes[E] = macro KebsPlayEnumMacros.materializeEnumUppercaseWrites[E]
  }

  trait Lowercase {
    implicit def enumReads[E <: EnumEntry]: Reads[E] = macro KebsPlayEnumMacros.materializeEnumLowercaseReads[E]
    implicit def enumFormat[E <: EnumEntry: InvariantDummy]: Writes[E] = macro KebsPlayEnumMacros.materializeEnumLowercaseWrites[E]
  }
}

object KebsEnumFormats extends KebsEnumFormats
