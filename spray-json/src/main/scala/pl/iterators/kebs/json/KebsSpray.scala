package pl.iterators.kebs.json

import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, JsonReader, RootJsonFormat}

trait KebsSpray extends CaseClass1ToValueClass { self: DefaultJsonProtocol =>
  import macros.KebsSprayMacros
  implicit def jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeRootFormat[T]
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

  final def jsonFormatRec[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeLazyFormat[T]

  @inline
  def _kebs_getField[T](value: JsValue, fieldName: String)(implicit reader: JsonReader[T]) = fromField[T](value, fieldName)
}

object KebsSpray {
  trait Snakified extends KebsSpray { self: DefaultJsonProtocol =>
    import macros.KebsSprayMacros
    implicit def snakifiedJsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.SnakifyVariant.materializeRootFormat[T]
  }
  trait NoFlat extends KebsSpray { self: DefaultJsonProtocol =>
    import macros.KebsSprayMacros
    implicit def noflat_jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.NoflatVariant.materializeRootFormat[T]
  }
  trait Capitalized extends KebsSpray { self: DefaultJsonProtocol =>
    import macros.KebsSprayMacros
    implicit def capitalizedJsonFormatN[T <: Product]: RootJsonFormat[T] =
      macro KebsSprayMacros.CapitalizedCamelCase.materializeRootFormat[T]
  }
}
