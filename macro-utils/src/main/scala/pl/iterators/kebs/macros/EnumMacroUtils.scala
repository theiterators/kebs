package pl.iterators.kebs.macros

import enumeratum.EnumEntry
import enumeratum.values.ValueEnumEntry

abstract class EnumMacroUtils extends MacroUtils {
  import c.universe._

  private val EnumEntry      = typeOf[EnumEntry]
  private val ValueEnumEntry = typeOf[ValueEnumEntry[_]]

  protected sealed abstract class EnumMapComap[E: c.WeakTypeTag, To] {
    final val E    = weakTypeOf[E]
    final def Enum = companion(E)
    def To: c.Type

    def map(entryName: TermName): Tree
    final def mapFunction: Tree = {
      val x = c.freshName(TermName("x"))
      q"($x: $E) => ${map(x)}"
    }
    def comap(valueName: TermName): Tree
    final def comapFunction: Tree = {
      val x = c.freshName(TermName("x"))
      q"($x: $To) => ${comap(x)}"
    }
    def comapOption(valueName: TermName): Tree

    def isValid: Boolean
    final def assertValid(msg: => String) = if (!isValid) c.abort(c.enclosingPosition, msg)
  }
  protected abstract class EnumEntryMapComap[E: c.WeakTypeTag] extends EnumMapComap[E, String] {
    override final val To      = typeOf[String]
    override final def isValid = E <:< EnumEntry
  }
  protected final class EntryNameInsensitive[E: c.WeakTypeTag] extends EnumEntryMapComap[E] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName"
    override def comap(valueName: TermName): Tree       = q"$Enum.withNameInsensitive($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameInsensitiveOption($valueName)"
  }
  protected final class EntryName[E: c.WeakTypeTag] extends EnumEntryMapComap[E] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName"
    override def comap(valueName: TermName): Tree       = q"$Enum.withName($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameOption($valueName)"
  }
  protected final class Lowercase[E: c.WeakTypeTag] extends EnumEntryMapComap[E] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName.toLowerCase"
    override def comap(valueName: TermName): Tree       = q"$Enum.withNameLowercaseOnly($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameLowercaseOnlyOption($valueName)"
  }
  protected final class Uppercase[E: c.WeakTypeTag] extends EnumEntryMapComap[E] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName.toUpperCase"
    override def comap(valueName: TermName): Tree       = q"$Enum.withNameUppercaseOnly($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameUppercaseOnlyOption($valueName)"
  }
  protected abstract class ValueEnumEntryMapComap[E: c.WeakTypeTag, ValueType] extends EnumMapComap[E, ValueType] {
    private val valueMethod    = E.member(TermName("value"))
    override final def To      = valueMethod.asMethod.returnType
    override final def isValid = E <:< ValueEnumEntry
  }
  protected final class Value[E: c.WeakTypeTag, ValueType] extends ValueEnumEntryMapComap[E, ValueType] {
    override def map(entryName: TermName): Tree     = q"$entryName.value"
    override def comap(value: TermName): Tree       = q"$Enum.withValue($value)"
    override def comapOption(value: TermName): Tree = q"$Enum.withValueOpt($value)"
  }

}
