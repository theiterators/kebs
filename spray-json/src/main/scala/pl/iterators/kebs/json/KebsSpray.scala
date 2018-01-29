package pl.iterators.kebs.json

import pl.iterators.kebs.macros.CaseClass1Rep
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, RootJsonFormat}

trait KebsSpray { self: DefaultJsonProtocol =>
  import macros.KebsSprayMacros
  implicit def jsonFormatN[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeRootFormat[T]
  implicit def jsonFlatFormat[T <: Product, A](implicit rep: CaseClass1Rep[T, A], baseJsonFormat: JsonFormat[A]): JsonFormat[T] = {
    val reader: JsValue => T = json => rep.apply(baseJsonFormat.read(json))
    val writer: T => JsValue = obj => baseJsonFormat.write(rep.unapply(obj))
    jsonFormat[T](reader, writer)
  }

  final def jsonFormatRec[T <: Product]: RootJsonFormat[T] = macro KebsSprayMacros.materializeLazyFormat[T]

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
