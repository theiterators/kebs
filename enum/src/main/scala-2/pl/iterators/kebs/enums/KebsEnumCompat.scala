package pl.iterators.kebs.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.enums.internal.EnumMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait KebsEnum {
  implicit def enumScala[E <: Enumeration#Value]: EnumLike[E] = macro EnumMacros.enumLikeImpl[E]
}

trait KebsValueEnum {
  implicit def kebsValueEnumScala[V, E <: ValueEnumLikeEntry[V]]: ValueEnumLike[V, E] = macro EnumMacros.valueEnumLikeImpl[V, E]
}

// an abort (not an enum of objects) only skips this implicit candidate
final private[enums] class EnumMacros(val c: blackbox.Context) extends hearth.MacroCommonsScala2 with EnumMacrosImpl {
  import c.universe._

  def enumLikeImpl[E: c.WeakTypeTag]: c.Expr[EnumLike[E]] = deriveEnumLike[E]

  def valueEnumLikeImpl[V: c.WeakTypeTag, E <: ValueEnumLikeEntry[V]: c.WeakTypeTag]: c.Expr[ValueEnumLike[V, E]] =
    c.Expr[ValueEnumLike[V, E]](
      q"_root_.pl.iterators.kebs.core.enums.ValueEnumLike.apply[${weakTypeOf[V]}, ${weakTypeOf[E]}](${enumCases[E].tree})"
    )
}
