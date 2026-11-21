package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}
import spray.json.{JsString, JsValue, JsonFormat}

trait KebsSprayJsonEnums {
  // mixing the base trait with a preset must not compile (it would silently pick one of them)
  protected def kebsEnumCodecs: Unit = ()

  @inline protected final def enumNameDeserializationError[E](`enum`: EnumLike[E], name: String) = {
    val enumNames = `enum`.names.mkString(", ")
    spray.json.deserializationError(s"$name should be one of $enumNames")
  }

  @inline protected final def enumValueDeserializationError[E](`enum`: EnumLike[E], value: JsValue) = {
    val enumNames = `enum`.names.mkString(", ")
    spray.json.deserializationError(s"$value should be a string of value $enumNames")
  }

  protected final def enumJsonFormat[E](`enum`: EnumLike[E], map: E => String, comap: String => Option[E]) = new JsonFormat[E] {
    override def write(obj: E): JsValue = JsString(map(obj))
    override def read(json: JsValue): E = json match {
      case JsString(name) => comap(name).getOrElse(enumNameDeserializationError(`enum`, name))
      case _              => enumValueDeserializationError(`enum`, json)
    }
  }

  def namedJsonFormat[E](`enum`: EnumLike[E], naming: EnumNaming): JsonFormat[E] = {
    val named = naming.applyTo(`enum`)
    enumJsonFormat[E](named, named.getName(_), naming.lookup(named, _))
  }

  def jsonFormat[E](`enum`: EnumLike[E]): JsonFormat[E]          = namedJsonFormat(`enum`, EnumNaming.default)
  def lowercaseJsonFormat[E](`enum`: EnumLike[E]): JsonFormat[E] = namedJsonFormat(`enum`, EnumNaming.lowercase)
  def uppercaseJsonFormat[E](`enum`: EnumLike[E]): JsonFormat[E] = namedJsonFormat(`enum`, EnumNaming.uppercase)

  implicit def jsonEnumFormat[E](implicit ev: EnumLike[E], naming: EnumNaming): JsonFormat[E] = namedJsonFormat(ev, naming)

  trait KebsSprayJsonEnumsUppercase {
    protected def kebsEnumCodecs: Unit = ()

    implicit def jsonEnumFormat[E](implicit ev: EnumLike[E]): JsonFormat[E] = uppercaseJsonFormat(ev)
  }

  trait KebsSprayJsonEnumsLowercase {
    protected def kebsEnumCodecs: Unit = ()

    implicit def jsonEnumFormat[E](implicit ev: EnumLike[E]): JsonFormat[E] = lowercaseJsonFormat(ev)
  }
}

trait KebsSprayJsonValueEnums {
  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E], value: V) = {
    val enumValues = `enum`.getValuesToEntriesMap.keys.mkString(", ")
    spray.json.deserializationError(s"$value is not a member of $enumValues")
  }

  def jsonFormatValue[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit baseJsonFormat: JsonFormat[V]) =
    new JsonFormat[E] {
      override def write(obj: E): JsValue = baseJsonFormat.write(obj.value)
      override def read(json: JsValue): E = {
        val value = baseJsonFormat.read(json)
        `enum`.withValueOption(value).getOrElse(valueEnumDeserializationError(`enum`, value))
      }
    }

  implicit def jsonValueEnumFormat[V, E <: ValueEnumLikeEntry[V]](implicit
      ev: ValueEnumLike[V, E],
      baseJsonFormat: JsonFormat[V]
  ): JsonFormat[E] = jsonFormatValue(ev)
}
