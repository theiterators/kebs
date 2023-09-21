package pl.iterators.kebs.enumeratum

import enumeratum.values._
import pl.iterators.kebs.enums.ValueEnumLike
import pl.iterators.kebs.macros.enums.EnumMacroUtils

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class ValueEnumOf[ValueType, E <: ValueEnumEntry[ValueType]](val valueEnum: ValueEnumLike[ValueType, E])

object ValueEnumOf {
  implicit def valueEnumOf[ValueType, E <: ValueEnumEntry[ValueType]]: ValueEnumOf[ValueType, E] = macro ValueEnumEntryMacros.valueEnumOfImpl[ValueType, E]
}

class ValueEnumEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
  import c.universe._

  def valueEnumOfImpl[ValueType: c.WeakTypeTag, E <: ValueEnumEntry[ValueType]: c.WeakTypeTag]: c.Expr[ValueEnumOf[ValueType, E]] = {
    val ValueType = weakTypeOf[ValueType]
    val EnumEntry = weakTypeOf[E]

    c.Expr[ValueEnumOf[ValueType, E]](
      q"""
        new _root_.pl.iterators.kebs.enumeratum.ValueEnumOf[${ValueType}, ${EnumEntry}](
          new _root_.pl.iterators.kebs.enums.ValueEnumLike[${ValueType}, ${EnumEntry}] {
            override def values: Seq[$EnumEntry] = ${companion(EnumEntry)}.values
          }
        )
      """
    )
//    c.Expr[ValueEnumOf[ValueType, E]](q"""new _root_.pl.iterators.kebs.enumeratum.ValueEnumOf[${weakTypeOf[ValueType]}, ${weakTypeOf[E]}](${companion(EnumEntry)}.values)""")
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
