package pl.iterators.kebs.macros

import enumeratum.EnumEntry

abstract class EnumMacroUtils extends MacroUtils {
  import c.universe._

  private val EnumEntry = typeOf[EnumEntry]

  protected def assertEnumEntry(t: Type, msg: => String) = if (!(t <:< EnumEntry)) c.abort(c.enclosingPosition, msg)

  protected sealed abstract class EnumMapComap[E: c.WeakTypeTag, To: c.TypeTag] {
    final val E    = weakTypeOf[E]
    final def Enum = companion(E)
    final def To   = typeOf[To]

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
  }
  protected final class EntryNameInsensitive[E: c.WeakTypeTag] extends EnumMapComap[E, String] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName"
    override def comap(valueName: TermName): Tree       = q"$Enum.withNameInsensitive($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameInsensitiveOption($valueName)"
  }
  protected final class EntryName[E: c.WeakTypeTag] extends EnumMapComap[E, String] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName"
    override def comap(valueName: TermName): Tree       = q"$Enum.withName($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameOption($valueName)"
  }
  protected final class Lowercase[E: c.WeakTypeTag] extends EnumMapComap[E, String] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName.toLowerCase"
    override def comap(valueName: TermName): Tree       = q"$Enum.withNameLowercaseOnly($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameLowercaseOnlyOption($valueName)"
  }
  protected final class Uppercase[E: c.WeakTypeTag] extends EnumMapComap[E, String] {
    override def map(entryName: TermName): Tree         = q"$entryName.entryName.toUpperCase"
    override def comap(valueName: TermName): Tree       = q"$Enum.withNameUppercaseOnly($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.withNameUppercaseOnlyOption($valueName)"
  }
  protected final class IndexOf[E: c.WeakTypeTag] extends EnumMapComap[E, Int] {
    override def map(entryName: TermName): Tree         = q"$Enum.indexOf($entryName)"
    override def comap(valueName: TermName): Tree       = q"$Enum.values($valueName)"
    override def comapOption(valueName: TermName): Tree = q"$Enum.values.lift($valueName)"
  }

}
