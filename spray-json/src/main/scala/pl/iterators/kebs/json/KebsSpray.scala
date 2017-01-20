package pl.iterators.kebs.json

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, RootJsonFormat}

trait KebsSpray { self: DefaultJsonProtocol =>
  import macros.KebsSprayMacros
  implicit def jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeRootFormat[T]
  implicit def jsonFlatFormat[T <: Product]: JsonFormat[T] = macro KebsSprayMacros.materializeFlatFormat[T]

  @inline final def constructJsonFormat[T](reader: JsValue => T, writer: T => JsValue) = jsonFormat(reader, writer)
}

object KebsSpray {
  trait Snakified extends KebsSpray { self: DefaultJsonProtocol =>
    import macros.KebsSprayMacros
    implicit def snakifiedJsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.SnakifyVariant.materializeRootFormat[T]
  }
}
