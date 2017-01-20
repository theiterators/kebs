package pl.iterators.kebs.json

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat}
import enumeratum.{Enum, EnumEntry}

private[json] trait EnumDeserializationErrors {
  @inline final def enumNameDeserializationError[E <: EnumEntry](enum: Enum[E], name: String) = {
    val enumNames = enum.namesToValuesMap.values.mkString(", ")
    spray.json.deserializationError(s"$name should be one of $enumNames")
  }

  @inline final def enumValueDeserializationError[E <: EnumEntry](enum: Enum[E], value: JsValue) = {
    val enumNames = enum.namesToValuesMap.values.mkString(", ")
    spray.json.deserializationError(s"$value should be a string of value $enumNames")
  }
}

trait KebsEnumFormats extends EnumDeserializationErrors { self: KebsSpray =>
  import macros.KebsSprayEnumMacros

  implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumFormat[E]

  trait Uppercase extends EnumDeserializationErrors {
    implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumUppercaseFormat[E]
  }

  trait Lowercase extends EnumDeserializationErrors {
    implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumLowercaseFormat[E]
  }
}

object KebsEnumFormats extends DefaultJsonProtocol with KebsSpray with KebsEnumFormats
