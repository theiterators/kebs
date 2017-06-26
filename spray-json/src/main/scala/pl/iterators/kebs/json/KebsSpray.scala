package pl.iterators.kebs.json

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, JsonReader, RootJsonFormat}

trait KebsSpray { self: DefaultJsonProtocol =>
  import macros.KebsSprayMacros
  implicit def jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeRootFormat[T]
  implicit def jsonFlatFormat[T <: Product]: JsonFormat[T] = macro KebsSprayMacros.materializeFlatFormat[T]

  final def jsonFormatRec[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeLazyFormat[T]
  @inline final def constructJsonFormat[T](reader: JsValue => T, writer: T => JsValue) = jsonFormat(reader, writer)

  implicit class PimpedJsValue(jsValue: JsValue) {
    def getField[T](fieldName: String)(implicit jr: JsonReader[T]): T = fromField[T](jsValue, fieldName)
  }
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
}
