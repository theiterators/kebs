//package pl.iterators.kebs.macros.enums
//
//import enumeratum.values._
//
//import scala.language.experimental.macros
//import scala.reflect.macros.blackbox
//
//class ValueEnumOf[ValueType, E <: ValueEnumEntry[ValueType]](val valueEnum: ValueEnum[ValueType, E])
//
//object ValueEnumOf {
//  implicit def intValueEnumOf[E <: IntEnumEntry]: ValueEnumOf[Int, E] =
//    macro ValueEnumEntryMacros.valueEnumOfImpl[Int, E]
//  implicit def shortValueEnumOf[E <: ShortEnumEntry]: ValueEnumOf[Short, E] =
//    macro ValueEnumEntryMacros.valueEnumOfImpl[Short, E]
//  implicit def longValueEnumOf[E <: LongEnumEntry]: ValueEnumOf[Long, E] =
//    macro ValueEnumEntryMacros.valueEnumOfImpl[Long, E]
//  implicit def byteValueEnumOf[E <: ByteEnumEntry]: ValueEnumOf[Byte, E] =
//    macro ValueEnumEntryMacros.valueEnumOfImpl[Byte, E]
//  implicit def stringValueEnumOf[E <: StringEnumEntry]: ValueEnumOf[String, E] =
//    macro ValueEnumEntryMacros.valueEnumOfImpl[String, E]
//}
//
///**
//  * this needs to be whitebox because macro needs to deduce `ValueType` type param
//  */
//class ValueEnumEntryMacros(override val c: blackbox.Context) extends EnumMacroUtils {
//  import c.universe._
//
//  def valueEnumOfImpl[ValueType: c.WeakTypeTag, E <: ValueEnumEntry[ValueType]: c.WeakTypeTag]: c.Expr[ValueEnumOf[ValueType, E]] = {
//    val EnumEntry = weakTypeOf[E]
//    assertValueEnumEntry(EnumEntry, s"${EnumEntry.typeSymbol} must subclass enumeratum.values.ValueEnumEntry")
//
//    val ValueType = weakTypeOf[ValueType]
//    c.Expr[ValueEnumOf[ValueType, E]](
//      q"new _root_.pl.iterators.kebs.macros.enums.ValueEnumOf[$ValueType, $EnumEntry](${companion(EnumEntry)})")
//  }
//}
