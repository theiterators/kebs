package pl.iterators.kebs.enumeratum

import enumeratum.values._
import pl.iterators.kebs.enums.{ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.macros.enums.EnumMacroUtils

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class ValueEnumOf[ValueType, E <: ValueEnumLikeEntry[ValueType] with ValueEnumEntry[ValueType]](val valueEnum: ValueEnumLike[ValueType, E])

object ValueEnumOf {
  implicit def intValueEnumOf[E <: IntEnumEntry with ValueEnumLikeEntry[Int]]: ValueEnumOf[Int, E] =
    macro ValueEnumEntryMacros.valueEnumOfImpl[Int, E]
  implicit def shortValueEnumOf[E <: ShortEnumEntry with ValueEnumLikeEntry[Short]]: ValueEnumOf[Short, E] =
    macro ValueEnumEntryMacros.valueEnumOfImpl[Short, E]
  implicit def longValueEnumOf[E <: LongEnumEntry with ValueEnumLikeEntry[Long]]: ValueEnumOf[Long, E] =
    macro ValueEnumEntryMacros.valueEnumOfImpl[Long, E]
  implicit def byteValueEnumOf[E <: ByteEnumEntry with ValueEnumLikeEntry[Byte]]: ValueEnumOf[Byte, E] =
    macro ValueEnumEntryMacros.valueEnumOfImpl[Byte, E]
  implicit def stringValueEnumOf[E <: StringEnumEntry with ValueEnumLikeEntry[String]]: ValueEnumOf[String, E] =
    macro ValueEnumEntryMacros.valueEnumOfImpl[String, E]
}

/**
  * this needs to be whitebox because macro needs to deduce `ValueType` type param
  */
class ValueEnumEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
  import c.universe._

  def valueEnumOfImpl[ValueType: c.WeakTypeTag, E <: ValueEnumEntry[ValueType] with ValueEnumLikeEntry[ValueType]: c.WeakTypeTag]
    : c.Expr[ValueEnumOf[ValueType, E]] = {
    val EnumEntry = weakTypeOf[E]
    assertValueEnumEntry(EnumEntry, s"${EnumEntry.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")

    val ValueType = weakTypeOf[ValueType]
    c.Expr[ValueEnumOf[ValueType, E]](
      q"new _root_.pl.iterators.kebs.macros.enums.ValueEnumOf[$ValueType, $EnumEntry](new _root_.pl.iterators.kebs.enums.ValueEnumLike[$ValueType, $EnumEntry] { override def values = ${companion(EnumEntry)}.values })")
  }
}
