package pl.iterators.kebs.enumeratum

import enumeratum.EnumEntry
import enumeratum.values.ValueEnumEntry
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.core.macros.internal.MirrorCases
import pl.iterators.kebs.enumeratum.internal.EnumeratumMacrosImpl

import scala.deriving.Mirror
import scala.quoted.*

// selected via the Mirror, so never abort: without an Enum companion the case objects are listed (kebs 2 behaviour)
trait KebsEnumeratum {
  inline implicit def enumLikeFromEnumeratum[E <: EnumEntry](using m: Mirror.SumOf[E]): EnumLike[E] = ${ EnumeratumMacros.enumLike[E] }
}

trait KebsValueEnumeratum {
  inline implicit def valueEnumeratumScala3[V, E <: ValueEnumEntry[V] & ValueEnumLikeEntry[V]](using
      m: Mirror.SumOf[E]
  ): ValueEnumLike[V, E] = ${ EnumeratumMacros.valueEnumLike[V, E] }
}

final private[enumeratum] class EnumeratumMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), EnumeratumMacrosImpl

private[enumeratum] object EnumeratumMacros {
  def enumLike[E <: EnumEntry: Type](using q: Quotes): Expr[EnumLike[E]] = {
    val macros = new EnumeratumMacros(q)
    macros.deriveEnumLike[E](macros.enumeratumEntries[E](MirrorCases.of[E]))
  }

  def valueEnumLike[V: Type, E <: ValueEnumEntry[V] & ValueEnumLikeEntry[V]: Type](using q: Quotes): Expr[ValueEnumLike[V, E]] = {
    val macros  = new EnumeratumMacros(q)
    val entries = macros.enumeratumEntries[E](MirrorCases.of[E])
    '{ ValueEnumLike[V, E]($entries) }
  }
}
