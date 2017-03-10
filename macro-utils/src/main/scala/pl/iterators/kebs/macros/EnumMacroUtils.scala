package pl.iterators.kebs.macros

import enumeratum.EnumEntry
import enumeratum.values.ValueEnumEntry

abstract class EnumMacroUtils extends MacroUtils {
  import c.universe._

  private val EnumEntry      = typeOf[EnumEntry]
  private val ValueEnumEntry = typeOf[ValueEnumEntry[_]]

  protected def assertEnumEntry(t: Type, msg: => String)      = if (!(t <:< EnumEntry)) c.abort(c.enclosingPosition, msg)
  protected def assertValueEnumEntry(t: Type, msg: => String) = if (!(t <:< ValueEnumEntry)) c.abort(c.enclosingPosition, msg)

  protected def ValueType(valueEnumEntry: Type) = valueEnumEntry.member(TermName("value")).asMethod.returnType
}
