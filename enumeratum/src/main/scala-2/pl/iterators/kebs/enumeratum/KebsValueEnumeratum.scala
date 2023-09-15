package pl.iterators.kebs.enumeratum

import enumeratum.values._
import pl.iterators.kebs.enums.{ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.macros.enums.EnumMacroUtils

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class ValueEnumOf[ValueType, E <: ValueEnumLikeEntry[ValueType]](val valueEnum: ValueEnumLike[ValueType, E])

object ValueEnumOf {
  implicit def intValueEnumOf[E <: IntEnumEntry, T <: ValueEnumLikeEntry[Int]]: ValueEnumOf[Int, T] = macro ValueEnumEntryMacros.valueEnumOfImpl[Int, E, T]
  implicit def shortValueEnumOf[E <: ShortEnumEntry, T <: ValueEnumLikeEntry[Short]]: ValueEnumOf[Short, T] = macro ValueEnumEntryMacros.valueEnumOfImpl[Short, E, T]
  implicit def longValueEnumOf[E <: LongEnumEntry, T <: ValueEnumLikeEntry[Long]]: ValueEnumOf[Long, T] = macro ValueEnumEntryMacros.valueEnumOfImpl[Long, E, T]
  implicit def byteValueEnumOf[E <: ByteEnumEntry, T <: ValueEnumLikeEntry[Byte]]: ValueEnumOf[Byte, T] = macro ValueEnumEntryMacros.valueEnumOfImpl[Byte, E, T]
  implicit def stringValueEnumOf[E <: StringEnumEntry, T <: ValueEnumLikeEntry[String]]: ValueEnumOf[String, T] = macro ValueEnumEntryMacros.valueEnumOfImpl[String, E, T]
}

class ValueEnumEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
  import c.universe._

  def valueEnumOfImpl[ValueType: c.WeakTypeTag, E <: ValueEnumEntry[ValueType]: c.WeakTypeTag, T <: ValueEnumLikeEntry[ValueType]: c.WeakTypeTag]: c.Expr[ValueEnumOf[ValueType, T]] = {
    val EnumeratumEntry = weakTypeOf[E]
    assertValueEnumEntry(EnumeratumEntry, s"${EnumeratumEntry.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")
    val EnumEntry = weakTypeOf[T]

    val ValueType = weakTypeOf[ValueType]
    c.Expr[ValueEnumOf[ValueType, T]](
      q"""
        new _root_.pl.iterators.kebs.enumeratum.ValueEnumOf[${ValueType}, ${EnumEntry}](
          new _root_.pl.iterators.kebs.enums.ValueEnumLike[${ValueType}, ${EnumEntry}] {
            override def values: Seq[$EnumEntry] = ${EnumeratumEntry.typeSymbol.companion}.values.map(item =>
              new _root_.pl.iterators.kebs.enums.ValueEnumLikeEntry[$ValueType] {
                override def value: $ValueType = item.value
              }
            )
          }
        )
      """
    )
  }
}

// This works in ValueEnumeratumTest!
//    val enumEntrySeq: Seq[ValueEnumLikeEntry[Int]] = LibraryItem.values.map(item =>
//      new ValueEnumLikeEntry[Int] {
//        override def value: Int = item.value
//      })
//    println(enumEntrySeq)


// I think there's something wrong with EnumeratumEntry usage since when quasiquote is repleced by
//    q"""
//      new _root_.pl.iterators.kebs.enumeratum.ValueEnumOf[${ValueType}, ${EnumEntry}](
//        new _root_.pl.iterators.kebs.enums.ValueEnumLike[${ValueType}, ${EnumEntry}] {
//          override def values: Seq[$EnumEntry] = null
//        }
//      )
//    """
// it suddenly compiles and the test just fails (but without any errors).
