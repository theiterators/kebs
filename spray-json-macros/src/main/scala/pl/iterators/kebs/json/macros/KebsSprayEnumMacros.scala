package pl.iterators.kebs.json.macros

import enumeratum.EnumEntry
import pl.iterators.kebs.macros.EnumMacroUtils
import spray.json.JsonFormat

import scala.reflect.macros._

class KebsSprayEnumMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  private def materializeJsonFormat[E](enumMapComap: EnumMapComap[E, _]) = {
    val E = enumMapComap.E
    assertEnumEntry(E, s"To materialize enum format ${E.typeSymbol} must subclass enumeratum.EnumEntry")

    val name           = TermName("name")
    val obj            = TermName("obj")
    val enum           = enumMapComap.Enum
    val maybeEnumEntry = enumMapComap.comapOption(name)

    val readerF =
      q"""(json: _root_.spray.json.JsValue) => json match {
          case _root_.spray.json.JsString($name) => $maybeEnumEntry.getOrElse(${_this}.enumNameDeserializationError[$E]($enum, $name))
          case _ => ${_this}.enumValueDeserializationError[$E]($enum, json)
        }
       """
    val writerF = q"($obj: $E) => _root_.spray.json.JsString(${enumMapComap.map(obj)})"

    c.Expr[JsonFormat[E]](q"${_this}.constructJsonFormat[$E]($readerF, $writerF)")
  }

  def materializeEnumFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]]          = materializeJsonFormat[E](new EntryNameInsensitive)
  def materializeEnumUppercaseFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = materializeJsonFormat[E](new Uppercase)
  def materializeEnumLowercaseFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = materializeJsonFormat[E](new Lowercase)

}
