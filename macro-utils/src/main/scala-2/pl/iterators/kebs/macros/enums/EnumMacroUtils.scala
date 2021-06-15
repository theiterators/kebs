package pl.iterators.kebs.macros.enums

import enumeratum.EnumEntry
import enumeratum.values.ValueEnumEntry
import pl.iterators.kebs.macros.MacroUtils

abstract class EnumMacroUtils extends MacroUtils {
  import c.universe._

  private val EnumEntry      = typeOf[EnumEntry]
  private val ValueEnumEntry = typeOf[ValueEnumEntry[_]]

  protected def assertEnumEntry(t: Type, msg: => String)      = if (!(t <:< EnumEntry)) c.abort(c.enclosingPosition, msg)
  protected def assertValueEnumEntry(t: Type, msg: => String) = if (!(t <:< ValueEnumEntry)) c.abort(c.enclosingPosition, msg)

  protected def ValueType(valueEnumEntry: Type) = valueEnumEntry.typeArgs.head
}
