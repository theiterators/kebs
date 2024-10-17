package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import spray.json.*
import scala.deriving._
import scala.compiletime._

case class FieldNamingStrategy(transform: String => String)

trait KebsSprayJson extends LowPriorityKebsSprayJson { self: DefaultJsonProtocol =>
  implicit def jsonFlatFormat[T, A](implicit rep: ValueClassLike[T, A], baseJsonFormat: JsonFormat[A]): JsonFormat[T] = {
    val reader: JsValue => T = json => rep.apply(baseJsonFormat.read(json))
    val writer: T => JsValue = obj => baseJsonFormat.write(rep.unapply(obj))
    jsonFormat[T](reader, writer)
  }
  implicit def jsonConversionFormat2[T, A](implicit rep: InstanceConverter[T, A], baseJsonFormat: JsonFormat[A]): JsonFormat[T] = {
    val reader: JsValue => T = json => rep.decode(baseJsonFormat.read(json))
    val writer: T => JsValue = obj => baseJsonFormat.write(rep.encode(obj))
    jsonFormat[T](reader, writer)
  }

  trait KebsSprayJsonSnakified extends KebsSprayJson { self: DefaultJsonProtocol =>
    import pl.iterators.kebs.core.macros.namingconventions.SnakifyVariant
    override implicit def namingStrategy: FieldNamingStrategy = FieldNamingStrategy(SnakifyVariant.snakify)
  }
  trait KebsSprayJsonCapitalized extends KebsSprayJson { self: DefaultJsonProtocol =>
    import pl.iterators.kebs.core.macros.namingconventions.CapitalizeVariant
    override implicit def namingStrategy: FieldNamingStrategy = FieldNamingStrategy(CapitalizeVariant.capitalize)
  }
}

trait LowPriorityKebsSprayJson {
  import macros.KebsSprayMacros
  implicit def namingStrategy: FieldNamingStrategy = FieldNamingStrategy(identity)

  def nullOptions: Boolean = this match {
    case _: NullOptions => true
    case _              => false
  }

  inline implicit def jsonFormatN[T <: Product](using m: Mirror.Of[T]): RootJsonFormat[T] = {
    KebsSprayMacros.materializeRootFormat[T](nullOptions)
  }

  inline final def jsonFormatRec[T <: Product](using m: Mirror.Of[T]): RootJsonFormat[T] =
    KebsSprayMacros.materializeRootFormat[T](nullOptions)
}
