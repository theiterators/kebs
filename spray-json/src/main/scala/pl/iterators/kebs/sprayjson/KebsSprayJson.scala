package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.naming.Naming
import spray.json.{deserializationError, DefaultJsonProtocol, JsValue, JsonFormat, NullOptions}

trait KebsSprayJson extends KebsSprayJsonAutoCompat { self: DefaultJsonProtocol =>
  // read through the protocol, not implicitly: another protocol imported in the same scope would make it ambiguous
  def kebsJsonConfig: JsonConfig =
    JsonConfig.default.withNoneHandling(if (this.isInstanceOf[NullOptions]) NoneHandling.WriteNull else NoneHandling.Omit)

  implicit def jsonFlatFormat[T, A](implicit rep: ValueClassLike[T, A], baseJsonFormat: JsonFormat[A]): JsonFormat[T] = {
    val reader: JsValue => T = json => rep.from(baseJsonFormat.read(json)).fold(deserializationError(_), identity)
    val writer: T => JsValue = obj => baseJsonFormat.write(rep.unapply(obj))
    jsonFormat[T](reader, writer)
  }
  implicit def jsonConversionFormat2[T, A](implicit rep: InstanceConverter[T, A], baseJsonFormat: JsonFormat[A]): JsonFormat[T] = {
    val reader: JsValue => T = json => rep.decodeEither(baseJsonFormat.read(json)).fold(deserializationError(_), identity)
    val writer: T => JsValue = obj => baseJsonFormat.write(rep.encode(obj))
    jsonFormat[T](reader, writer)
  }

  trait KebsSprayJsonSnakified extends KebsSprayJson { self: DefaultJsonProtocol =>
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }
  trait KebsSprayJsonCapitalized extends KebsSprayJson { self: DefaultJsonProtocol =>
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.pascalCase)
  }
}
