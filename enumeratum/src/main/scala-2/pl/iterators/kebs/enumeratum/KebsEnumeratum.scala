package pl.iterators.kebs.enumeratum

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import enumeratum.EnumEntry
import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.macros.MacroUtils

trait KebsEnumeratum {
  implicit def enumeratumScala2[E <: EnumEntry]: EnumLike[E] = macro EnumeratumEntryMacros.enumeratumOfImpl[E]
}

class EnumeratumEntryMacros(val c: blackbox.Context) extends MacroUtils {
  import c.universe._

  private def assertEnumEntry(t: Type, msg: => String) = if (!(t <:< typeOf[EnumEntry])) c.abort(c.enclosingPosition, msg)

  def enumeratumOfImpl[E <: EnumEntry: c.WeakTypeTag]: c.Expr[EnumLike[E]] = {
    val EnumEntry = weakTypeOf[E]
    assertEnumEntry(EnumEntry, s"${EnumEntry.typeSymbol} must subclass EnumEntry")

    c.Expr[EnumLike[E]](
      q"new _root_.pl.iterators.kebs.core.enums.EnumLike[${EnumEntry.typeSymbol}] { override def valuesToNamesMap: Map[${EnumEntry.typeSymbol}, String] = ${companion(EnumEntry)}.values.map(v => v -> v.entryName).toMap }"
    )
  }
}
