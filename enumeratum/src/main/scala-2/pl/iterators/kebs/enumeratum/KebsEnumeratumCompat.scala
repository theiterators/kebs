package pl.iterators.kebs.enumeratum

import enumeratum.EnumEntry
import enumeratum.values._
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.enumeratum.internal.EnumeratumMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait KebsEnumeratum {
  implicit def enumeratumScala2[E <: EnumEntry]: EnumLike[E] = macro EnumeratumMacros.enumLikeImpl[E]
}

trait KebsValueEnumeratum {
  // format: off
  implicit def valueIntEnumeratumScala2[E <: IntEnumEntry with ValueEnumLikeEntry[Int]]: ValueEnumLike[Int, E] = macro EnumeratumMacros.valueEnumLikeImpl[Int, E]
  implicit def valueShortEnumeratumScala2[E <: ShortEnumEntry with ValueEnumLikeEntry[Short]]: ValueEnumLike[Short, E] = macro EnumeratumMacros.valueEnumLikeImpl[Short, E]
  implicit def valueLongEnumeratumScala2[E <: LongEnumEntry with ValueEnumLikeEntry[Long]]: ValueEnumLike[Long, E] = macro EnumeratumMacros.valueEnumLikeImpl[Long, E]
  implicit def valueByteEnumeratumScala2[E <: ByteEnumEntry with ValueEnumLikeEntry[Byte]]: ValueEnumLike[Byte, E] = macro EnumeratumMacros.valueEnumLikeImpl[Byte, E]
  implicit def valueStringEnumeratumScala2[E <: StringEnumEntry with ValueEnumLikeEntry[String]]: ValueEnumLike[String, E] = macro EnumeratumMacros.valueEnumLikeImpl[String, E]
  implicit def valueEnumeratumScala2[ValueType, E <: ValueEnumEntry[ValueType] with ValueEnumLikeEntry[ValueType]]: ValueEnumLike[ValueType, E] = macro EnumeratumMacros.valueEnumLikeImpl[ValueType, E]
  // format: on
}

// an abort (e.g. no Enum companion) only skips this implicit candidate
final private[enumeratum] class EnumeratumMacros(val c: blackbox.Context) extends hearth.MacroCommonsScala2 with EnumeratumMacrosImpl {
  import c.universe._

  def enumLikeImpl[E <: EnumEntry: c.WeakTypeTag]: c.Expr[EnumLike[E]] = deriveEnumLike[E](enumeratumEntries[E](None))

  def valueEnumLikeImpl[V: c.WeakTypeTag, E <: ValueEnumEntry[V] with ValueEnumLikeEntry[V]: c.WeakTypeTag]
      : c.Expr[ValueEnumLike[V, E]] = {
    val entries = enumeratumEntries[E](None)
    c.Expr[ValueEnumLike[V, E]](
      q"_root_.pl.iterators.kebs.core.enums.ValueEnumLike.apply[${weakTypeOf[V]}, ${weakTypeOf[E]}](${entries.tree})"
    )
  }
}
