package pl.iterators.kebs.json

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, RootJsonFormat}

trait KebsSpray { self: DefaultJsonProtocol =>
  import macros.KebsSprayMacros
  implicit def jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeRootFormat[T]
  implicit def jsonFlatFormat[T <: Product]: JsonFormat[T] = macro KebsSprayMacros.materializeFlatFormat[T]

  final def jsonFormatRec[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeLazyFormat[T]
  @inline final def constructJsonFormat[T](reader: JsValue => T, writer: T => JsValue) = jsonFormat(reader, writer)

  implicit class PimpedJsValue(jsValue: JsValue) {
    def _kebs_getField[T](fieldName: String)(implicit jf: JsonFormat[T]): T = fromField[T](jsValue, fieldName)
  }
  implicit class PimpedAny[T](any: T) {
    def _kebs_toJson(implicit jf: JsonFormat[T]): JsValue = jf.write(any)
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
