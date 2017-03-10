package pl.iterators.kebs.json.macros

import pl.iterators.kebs.macros.EnumMacroUtils
import spray.json.JsonFormat

import scala.reflect.macros._

class KebsSprayEnumMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  private def materializeJsonFormat[E](enumMapComap: EnumEntryMapComap[E]) = {
    val E = enumMapComap.E
    enumMapComap.assertValid(s"To materialize enum format ${E.typeSymbol} must subclass enumeratum.EnumEntry")

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

  private def jsonFormatOf(p: Type) = appliedType(typeOf[JsonFormat[_]], p)

  private def materializeJsonFormat[E](enumMapComap: ValueEnumEntryMapComap[E, _]) = {
    val E = enumMapComap.E
    enumMapComap.assertValid(s"To materialize value enum format ${E.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")

    val ValueType = enumMapComap.To
    val baseJsonFormat =
      inferImplicitValue(jsonFormatOf(ValueType), s"To materialize value enum format ${E.typeSymbol}, JsonFormat[$ValueType] is needed")

    val value = TermName("value")
    val obj   = TermName("obj")
    val enum  = enumMapComap.Enum

    val readerF =
      q"""(json: _root_.spray.json.JsValue) => {
          val $value = $baseJsonFormat.read(json)
          ${enumMapComap.comapOption(value)}.getOrElse(${_this}.valueEnumDeserializationError[$ValueType, $E]($enum, $value))
        }"""
    val writerF = q"($obj: $E) => $baseJsonFormat.write(${enumMapComap.map(obj)})"

    c.Expr[JsonFormat[E]](q"${_this}.constructJsonFormat[$E]($readerF, $writerF)")
  }

  def materializeEnumFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]]          = materializeJsonFormat[E](new EntryNameInsensitive)
  def materializeEnumUppercaseFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = materializeJsonFormat[E](new Uppercase)
  def materializeEnumLowercaseFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = materializeJsonFormat[E](new Lowercase)
  def materializeValueEnumFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]]     = materializeJsonFormat[E](new Value)
}
