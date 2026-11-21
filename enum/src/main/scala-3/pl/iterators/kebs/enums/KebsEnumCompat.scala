package pl.iterators.kebs.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.enums.internal.EnumMacrosImpl

import scala.deriving.Mirror
import scala.quoted.*

trait KebsEnum {
  inline implicit def kebsEnumScala[E <: scala.reflect.Enum](using m: Mirror.SumOf[E]): EnumLike[E] = ${ EnumMacros.enumLike[E] }
  inline implicit def kebsEnumerationScala[E <: Enumeration#Value]: EnumLike[E]                     = ${ EnumMacros.enumLike[E] }
}

trait KebsValueEnum {
  inline implicit def kebsValueEnumScala[V, E <: ValueEnumLikeEntry[V]](using m: Mirror.SumOf[E]): ValueEnumLike[V, E] = ${
    EnumMacros.valueEnumLike[V, E]
  }
}

final private[enums] class EnumMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), EnumMacrosImpl

private[enums] object EnumMacros {
  def enumLike[E: Type](using q: Quotes): Expr[EnumLike[E]] = new EnumMacros(q).deriveEnumLike[E]

  def valueEnumLike[V: Type, E <: ValueEnumLikeEntry[V]: Type](using q: Quotes): Expr[ValueEnumLike[V, E]] = {
    val cases = new EnumMacros(q).enumCases[E]
    '{ ValueEnumLike[V, E]($cases) }
  }
}
