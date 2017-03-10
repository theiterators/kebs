package pl.iterators.kebs.json.macros

import pl.iterators.kebs.macros.EnumMacroUtils
import spray.json.JsonFormat

import scala.reflect.macros._

class KebsSprayEnumMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  def materializeEnumFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize enum format ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[JsonFormat[E]](q"${_this}.jsonFormat[$EnumEntry](${companion(EnumEntry)})")
  }
  def materializeEnumUppercaseFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize enum format ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[JsonFormat[E]](q"${_this}.uppercaseJsonFormat[$EnumEntry](${companion(EnumEntry)})")
  }
  def materializeEnumLowercaseFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize enum format ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[JsonFormat[E]](q"${_this}.lowercaseJsonFormat[$EnumEntry](${companion(EnumEntry)})")
  }
  def materializeValueEnumFormat[E: c.WeakTypeTag]: c.Expr[JsonFormat[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertValueEnumEntry(EnumEntry,
                         s"To materialize value enum format ${EnumEntry.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")

    c.Expr[JsonFormat[E]](q"${_this}.jsonFormat[${ValueType(EnumEntry)}, $EnumEntry](${companion(EnumEntry)})")
  }
}
