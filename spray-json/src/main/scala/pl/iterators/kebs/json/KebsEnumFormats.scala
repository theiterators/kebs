package pl.iterators.kebs.json

import enumeratum.values.{ValueEnum, ValueEnumEntry}
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

  @inline final def valueEnumDeserializationError[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E], value: V) = {
    val enumValues = enum.valuesToEntriesMap.keys.mkString(", ")
    spray.json.deserializationError(s"$value is not a member of $enumValues")
  }
}

trait KebsEnumFormats extends EnumDeserializationErrors { self: KebsSpray =>
  import macros.KebsSprayEnumMacros

  implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumFormat[E]
  implicit def jsonValueEnumFormat[E <: ValueEnumEntry[_]]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeValueEnumFormat[E]

  trait Uppercase extends EnumDeserializationErrors {
    implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumUppercaseFormat[E]
  }

  trait Lowercase extends EnumDeserializationErrors {
    implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumLowercaseFormat[E]
  }
}

object KebsEnumFormats extends DefaultJsonProtocol with KebsSpray with KebsEnumFormats
