package pl.iterators.kebs.json

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, RootJsonFormat}

trait KebsSpray { self: DefaultJsonProtocol =>
  import macros.KebsSprayMacros
  implicit def jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeRootFormat[T]
  implicit def jsonFlatFormat[T <: Product]: JsonFormat[T] = macro KebsSprayMacros.materializeFlatFormat[T]

  final def jsonFormatRec[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeLazyFormat[T]
  @inline final def constructJsonFormat[T](reader: JsValue => T, writer: T => JsValue) = jsonFormat(reader, writer)
}

object KebsSpray {
  trait Snakified extends KebsSpray { self: DefaultJsonProtocol =>
    import macros.KebsSprayMacros
    implicit def snakifiedJsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.SnakifyVariant.materializeRootFormat[T]
  }
  trait NoFlat extends KebsSpray { self: DefaultJsonProtocol =>
    import macros.KebsSprayMacros
    override implicit def jsonFormatN[T <: Product] = macro KebsSprayMacros.NoflatVariant.materializeRootFormat[T]
  }
}
