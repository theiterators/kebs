package pl.iterators.kebs.enums.macros

import pl.iterators.kebs.macros.EnumMacroUtils
import slick.lifted.Isomorphism

import scala.reflect.macros._

class KebsEnumMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  def materializeEnumColumn[E: c.WeakTypeTag]: c.Expr[Isomorphism[E, String]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize column type ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[Isomorphism[E, String]](q"${_this}.enumIsomorphism[$EnumEntry](${companion(EnumEntry)})")
  }

  def materializeEnumColumnWithNameLowercase[E: c.WeakTypeTag]: c.Expr[Isomorphism[E, String]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize column type ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[Isomorphism[E, String]](q"${_this}.lowercaseEnumIsomorphism[$EnumEntry](${companion(EnumEntry)})")
  }

  def materializeEnumColumnWithNameUppercase[E: c.WeakTypeTag]: c.Expr[Isomorphism[E, String]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"To materialize column type ${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[Isomorphism[E, String]](q"${_this}.uppercaseEnumIsomorphism[$EnumEntry](${companion(EnumEntry)})")
  }

  def materializeValueEnumColumn[E: c.WeakTypeTag, V]: c.Expr[Isomorphism[E, V]] = {
    val EnumEntry = weakTypeOf[E]
    assertValueEnumEntry(EnumEntry, s"To materialize column type ${EnumEntry.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")

    val V = ValueType(EnumEntry)
    c.Expr[Isomorphism[E, V]](q"${_this}.valueEnumIsomorphism[$V, $EnumEntry](${companion(EnumEntry)})")
  }

}
