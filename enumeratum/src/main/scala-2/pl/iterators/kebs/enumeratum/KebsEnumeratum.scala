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

    val Companion = companion(EnumEntry)
    // e.g. a case object's own type: there is no enum companion to take values from
    if (Companion == NoSymbol) c.abort(c.enclosingPosition, s"${EnumEntry.typeSymbol} has no companion object")

    c.Expr[EnumLike[E]](
      q"_root_.pl.iterators.kebs.core.enums.EnumLike[$EnumEntry]($Companion.values.toList, (e: $EnumEntry) => e.entryName)"
    )
  }
}
