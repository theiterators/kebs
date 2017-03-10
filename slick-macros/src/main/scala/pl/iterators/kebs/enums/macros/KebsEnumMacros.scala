package pl.iterators.kebs.enums.macros

import pl.iterators.kebs.macros.EnumMacroUtils
import slick.lifted.Isomorphism

import scala.reflect.macros._

class KebsEnumMacros(override val c: whitebox.Context) extends EnumMacroUtils {
  import c.universe._

  private def materializeIsomorphism[E, T](enumMapComap: EnumMapComap[E, T]) = {
    val E = enumMapComap.E
    enumMapComap.assertValid(
      s"To materialize column type ${E.typeSymbol} must subclass enumeratum.EnumEntry or enumeratum.values.ValueEnumEntry")

    val to    = enumMapComap.To
    val map   = enumMapComap.mapFunction
    val comap = enumMapComap.comapFunction

    c.Expr[Isomorphism[E, T]](q"new _root_.slick.lifted.Isomorphism[$E, $to]($map, $comap)")
  }

  def materializeEnumColumn[E: c.WeakTypeTag]: c.Expr[Isomorphism[E, String]] = materializeIsomorphism[E, String](new EntryName)

  def materializeEnumColumnWithNameLowercase[E: c.WeakTypeTag]: c.Expr[Isomorphism[E, String]] =
    materializeIsomorphism[E, String](new Lowercase)

  def materializeEnumColumnWithNameUppercase[E: c.WeakTypeTag]: c.Expr[Isomorphism[E, String]] =
    materializeIsomorphism[E, String](new Uppercase)

  def materializeValueEnumColumn[E: c.WeakTypeTag, V]: c.Expr[Isomorphism[E, V]] = materializeIsomorphism[E, V](new Value)

}
