package pl.iterators.kebs.json

import enumeratum.values.{ValueEnum, ValueEnumEntry}
import enumeratum.{Enum, EnumEntry}
import spray.json.{JsString, JsValue, JsonFormat}

trait SprayJsonEnum {
  @inline protected final def enumNameDeserializationError[E <: EnumEntry](enum: Enum[E], name: String) = {
    val enumNames = enum.namesToValuesMap.values.mkString(", ")
    spray.json.deserializationError(s"$name should be one of $enumNames")
  }

  @inline protected final def enumValueDeserializationError[E <: EnumEntry](enum: Enum[E], value: JsValue) = {
    val enumNames = enum.namesToValuesMap.values.mkString(", ")
    spray.json.deserializationError(s"$value should be a string of value $enumNames")
  }

  protected final def enumJsonFormat[E <: EnumEntry](enum: Enum[E], map: E => String, comap: String => Option[E]) = new JsonFormat[E] {
    override def write(obj: E): JsValue = JsString(map(obj))
    override def read(json: JsValue): E = json match {
      case JsString(name) => comap(name).getOrElse(enumNameDeserializationError(enum, name))
      case _              => enumValueDeserializationError(enum, json)
    }
  }
  def jsonFormat[E <: EnumEntry](enum: Enum[E]) = enumJsonFormat[E](enum, _.entryName, enum.withNameInsensitiveOption(_))
  def lowercaseJsonFormat[E <: EnumEntry](enum: Enum[E]) =
    enumJsonFormat[E](enum, _.entryName.toLowerCase, enum.withNameLowercaseOnlyOption(_))
  def uppercaseJsonFormat[E <: EnumEntry](enum: Enum[E]) =
    enumJsonFormat[E](enum, _.entryName.toUpperCase, enum.withNameUppercaseOnlyOption(_))
}

trait SprayJsonValueEnum {
  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E], value: V) = {
    val enumValues = enum.valuesToEntriesMap.keys.mkString(", ")
    spray.json.deserializationError(s"$value is not a member of $enumValues")
  }

  def jsonFormat[V, E <: ValueEnumEntry[V]](enum: ValueEnum[V, E])(implicit baseJsonFormat: JsonFormat[V]) = new JsonFormat[E] {
    override def write(obj: E): JsValue = baseJsonFormat.write(obj.value)
    override def read(json: JsValue): E = {
      val value = baseJsonFormat.read(json)
      enum.withValueOpt(value).getOrElse(valueEnumDeserializationError(enum, value))
    }
  }
}

trait KebsEnumFormats extends SprayJsonEnum with SprayJsonValueEnum {
  import macros.KebsSprayEnumMacros

  implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumFormat[E]
  implicit def jsonValueEnumFormat[E <: ValueEnumEntry[_]]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeValueEnumFormat[E]

  trait Uppercase extends SprayJsonEnum {
    implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumUppercaseFormat[E]
  }

  trait Lowercase extends SprayJsonEnum {
    implicit def jsonEnumFormat[E <: EnumEntry]: JsonFormat[E] = macro KebsSprayEnumMacros.materializeEnumLowercaseFormat[E]
  }
}

object KebsEnumFormats extends KebsEnumFormats
