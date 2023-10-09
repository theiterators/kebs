package pl.iterators.kebs.enumeratum

import pl.iterators.kebs.enums.EnumLike
import pl.iterators.kebs.macros.enums.EnumMacroUtils

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.blackbox
import enumeratum.EnumEntry

trait KebsEnumeratum {
  implicit def enumeratumScala2[E <: EnumEntry]: EnumLike[E] = macro EnumeratumEntryMacros.enumeratumOfImpl[E]
}

class EnumeratumEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
  import c.universe._

  def enumeratumOfImpl[E <: EnumEntry: c.WeakTypeTag]: c.Expr[EnumLike[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"${EnumEntry.typeSymbol} must subclass EnumEntry")

    c.Expr[EnumLike[E]](q"new _root_.pl.iterators.kebs.enums.EnumLike[${EnumEntry.typeSymbol}] { override def values: Seq[${EnumEntry.typeSymbol}] = ${companion(EnumEntry)}.values.toSeq }")
  }
}


