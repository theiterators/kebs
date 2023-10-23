package pl.iterators.kebs.enumeratum

import enumeratum.values._
import pl.iterators.kebs.enums.ValueEnumLike

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait KebsValueEnumeratum {
  implicit def valueIntEnumeratumScala2[E <: IntEnumEntry]: ValueEnumLike[Int, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Int, E]
  implicit def valueShortEnumeratumScala2[E <: ShortEnumEntry]: ValueEnumLike[Short, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Short, E]
  implicit def valueLongEnumeratumScala2[E <: LongEnumEntry]: ValueEnumLike[Long, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Long, E]
  implicit def valueByteEnumeratumScala2[E <: ByteEnumEntry]: ValueEnumLike[Byte, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Byte, E]
  implicit def valueStringEnumeratumScala2[E <: StringEnumEntry]: ValueEnumLike[String, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[String, E]
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
