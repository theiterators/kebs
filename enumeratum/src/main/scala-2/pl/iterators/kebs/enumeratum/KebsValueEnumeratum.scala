package pl.iterators.kebs.enumeratum

import enumeratum.values._
import pl.iterators.kebs.enums.ValueEnumLike

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait KebsValueEnumeratum {
  implicit def valueEnumeratumScala2[ValueType, E <: ValueEnumEntry[ValueType]]: ValueEnumLike[ValueType, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[ValueType, E]
}


class ValueEnumEntryMacros(val c: blackbox.Context) {
  import c.universe._

  def valueEnumOfImpl[ValueType: c.WeakTypeTag, E <: ValueEnumEntry[ValueType]: c.WeakTypeTag]: c.Expr[ValueEnumLike[ValueType, E]] = {
    val ValueType = weakTypeOf[ValueType]
    val EnumEntry = weakTypeOf[E]

    c.Expr[ValueEnumLike[ValueType, E]](
      q"""
        new _root_.pl.iterators.kebs.enums.ValueEnumLike[${ValueType}, ${EnumEntry}] {
          override def values: Seq[$EnumEntry] = ${EnumEntry.typeSymbol.companion}.values.toSeq
        }
      """
    )
  }
}
