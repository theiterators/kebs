package pl.iterators.kebs.json

import pl.iterators.kebs.enums.{EnumLike, ValueEnumLike}
import spray.json.{JsString, JsValue, JsonFormat}

trait SprayJsonEnum {
  @inline protected final def enumNameDeserializationError[E](`enum`: EnumLike[E], name: String) = {
    val enumNames = `enum`.namesToValuesMap.values.mkString(", ")
    spray.json.deserializationError(s"$name should be one of $enumNames")
  }

  @inline protected final def enumValueDeserializationError[E](`enum`: EnumLike[E], value: JsValue) = {
    val enumNames = `enum`.namesToValuesMap.values.mkString(", ")
    spray.json.deserializationError(s"$value should be a string of value $enumNames")
  }

  protected final def enumJsonFormat[E](`enum`: EnumLike[E], map: E => String, comap: String => Option[E]) = new JsonFormat[E] {
    override def write(obj: E): JsValue = JsString(map(obj))
    override def read(json: JsValue): E = json match {
      case JsString(name) => comap(name).getOrElse(enumNameDeserializationError(`enum`, name))
      case _              => enumValueDeserializationError(`enum`, json)
    }
  }
  def jsonFormat[E](`enum`: EnumLike[E]) = enumJsonFormat[E](`enum`, _.toString, `enum`.withNameInsensitiveOption(_))
  def lowercaseJsonFormat[E](`enum`: EnumLike[E]) =
    enumJsonFormat[E](`enum`, _.toString.toLowerCase, `enum`.withNameLowercaseOnlyOption(_))
  def uppercaseJsonFormat[E](`enum`: EnumLike[E]) =
    enumJsonFormat[E](`enum`, _.toString.toUpperCase, `enum`.withNameUppercaseOnlyOption(_))
}

trait SprayJsonValueEnum {
  @inline protected final def valueEnumDeserializationError[V, E <: { def value: V }](`enum`: ValueEnumLike[V, E], value: V) = {
    val enumValues = `enum`.valuesToEntriesMap.keys.mkString(", ")
    spray.json.deserializationError(s"$value is not a member of $enumValues")
  }

  def jsonFormatValue[V, E <: { def value: V }](`enum`: ValueEnumLike[V, E])(implicit baseJsonFormat: JsonFormat[V]) = new JsonFormat[E] {
    override def write(obj: E): JsValue = baseJsonFormat.write(obj.value)
    override def read(json: JsValue): E = {
      val value = baseJsonFormat.read(json)
      `enum`.withValueOpt(value).getOrElse(valueEnumDeserializationError(`enum`, value))
    }
  }
}

trait KebsEnumFormats extends SprayJsonEnum with SprayJsonValueEnum {
  implicit def jsonEnumFormat[E](implicit ev: EnumLike[E]): JsonFormat[E] = jsonFormat(ev)
  implicit def jsonValueEnumFormat[V, E <: { def value: V }](implicit ev: ValueEnumLike[V, E],
                                                              baseJsonFormat: JsonFormat[V]): JsonFormat[E] = jsonFormatValue(ev)

  trait Uppercase extends SprayJsonEnum {
    implicit def jsonEnumFormat[E](implicit ev: EnumLike[E]): JsonFormat[E] = uppercaseJsonFormat(ev)
  }

  trait Lowercase extends SprayJsonEnum {
    implicit def jsonEnumFormat[E](implicit ev: EnumLike[E]): JsonFormat[E] = lowercaseJsonFormat(ev)
  }
}

object KebsEnumFormats extends KebsEnumFormats
