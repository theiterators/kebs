package pl.iterators.kebs.enums

import pl.iterators.kebs.macros.enums.EnumMacroUtils

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.blackbox

trait KebsEnum {
  implicit def enumScala2[E <: Enumeration]: EnumLike[E] = macro EnumerationEntryMacros.enumOfImpl[E]
}

class EnumerationEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
  import c.universe._

  def enumOfImpl[E <: Enumeration: c.WeakTypeTag]: c.Expr[EnumLike[E]] = {
    val EnumerationEntry = weakTypeOf[E]
//    assertEnumEntry(EnumerationEntry, s"${EnumerationEntry.typeSymbol} must subclass Enumeration")

    c.Expr[EnumLike[E]](q"new _root_.pl.iterators.kebs.enums.EnumLike[${EnumerationEntry.typeSymbol}] { override def values: Array[${EnumerationEntry.typeSymbol}] = ${EnumerationEntry}.values.toArray }")
  }
}

