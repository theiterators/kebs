package pl.iterators.kebs.enumeratum

import enumeratum.values._
import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait KebsValueEnumeratum {
  implicit def valueIntEnumeratumScala2[E <: IntEnumEntry with ValueEnumLikeEntry[Int]]: ValueEnumLike[Int, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Int, E]
  implicit def valueShortEnumeratumScala2[E <: ShortEnumEntry with ValueEnumLikeEntry[Short]]: ValueEnumLike[Short, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Short, E]
  implicit def valueLongEnumeratumScala2[E <: LongEnumEntry with ValueEnumLikeEntry[Long]]: ValueEnumLike[Long, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Long, E]
  implicit def valueByteEnumeratumScala2[E <: ByteEnumEntry with ValueEnumLikeEntry[Byte]]: ValueEnumLike[Byte, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[Byte, E]
  implicit def valueStringEnumeratumScala2[E <: StringEnumEntry with ValueEnumLikeEntry[String]]: ValueEnumLike[String, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[String, E]
  implicit def valueEnumeratumScala2[ValueType, E <: ValueEnumEntry[ValueType] with ValueEnumLikeEntry[ValueType]]: ValueEnumLike[ValueType, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[ValueType, E]
}


class ValueEnumEntryMacros(val c: blackbox.Context) {
  import c.universe._

  def valueEnumOfImpl[ValueType: c.WeakTypeTag, E <: ValueEnumEntry[ValueType] with ValueEnumLikeEntry[ValueType]: c.WeakTypeTag]: c.Expr[ValueEnumLike[ValueType, E]] = {
    val ValueType = weakTypeOf[ValueType]
    val EnumEntry = weakTypeOf[E]

    c.Expr[ValueEnumLike[ValueType, E]](
      q"""
        new _root_.pl.iterators.kebs.core.enums.ValueEnumLike[${ValueType}, ${EnumEntry}] {
          override def values: Seq[$EnumEntry] = ${EnumEntry.typeSymbol.companion}.values.toSeq
        }
      """
    )
  }
}
