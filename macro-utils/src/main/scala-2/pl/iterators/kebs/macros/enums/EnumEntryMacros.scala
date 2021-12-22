package pl.iterators.kebs.macros.enums

import enumeratum.{Enum, EnumEntry}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class EnumOf[E <: EnumEntry](val `enum`: Enum[E])

object EnumOf {
  implicit def enumOf[E <: EnumEntry]: EnumOf[E] = macro EnumEntryMacros.enumOfImpl[E]
}

class EnumEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
  import c.universe._

  def enumOfImpl[E <: EnumEntry: c.WeakTypeTag]: c.Expr[EnumOf[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"${EnumEntry.typeSymbol} must subclass enumeratum.EnumEntry")

    c.Expr[EnumOf[E]](q"new _root_.pl.iterators.kebs.macros.enums.EnumOf(${companion(EnumEntry)})")
  }
}
