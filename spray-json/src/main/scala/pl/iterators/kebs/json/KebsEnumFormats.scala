package pl.iterators.kebs.json

import enumeratum.values.{ValueEnum, ValueEnumEntry}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.macros.enums.{EnumOf, ValueEnumOf}
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
  implicit def jsonEnumFormat[E <: EnumEntry](implicit ev: EnumOf[E]): JsonFormat[E] = jsonFormat(ev.enum)
  implicit def jsonValueEnumFormat[V, E <: ValueEnumEntry[V]](implicit ev: ValueEnumOf[V, E],
                                                              baseJsonFormat: JsonFormat[V]): JsonFormat[E] = jsonFormat(ev.valueEnum)

  trait Uppercase extends SprayJsonEnum {
    implicit def jsonEnumFormat[E <: EnumEntry](implicit ev: EnumOf[E]): JsonFormat[E] = uppercaseJsonFormat(ev.enum)
  }

  trait Lowercase extends SprayJsonEnum {
    implicit def jsonEnumFormat[E <: EnumEntry](implicit ev: EnumOf[E]): JsonFormat[E] = lowercaseJsonFormat(ev.enum)
  }
}

object KebsEnumFormats extends KebsEnumFormats
