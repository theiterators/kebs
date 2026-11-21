package pl.iterators.kebs.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.enums.internal.EnumMacrosImpl

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

trait KebsEnum {
  implicit def enumScala[E <: Enumeration#Value]: EnumLike[E] = macro EnumMacros.enumerationLikeImpl[E]
}

// the fixed value types let Scala 2 infer V in codecs shaped like valueEnumCodec[V, E <: ValueEnumLikeEntry[V]]
trait KebsValueEnum {
  // format: off
  implicit def kebsValueEnumScala[V, E <: ValueEnumLikeEntry[V]]: ValueEnumLike[V, E] = macro EnumMacros.valueEnumLikeImpl[V, E]
  implicit def kebsIntValueEnumScala[E <: ValueEnumLikeEntry[Int]]: ValueEnumLike[Int, E] = macro EnumMacros.valueEnumLikeImpl[Int, E]
  implicit def kebsLongValueEnumScala[E <: ValueEnumLikeEntry[Long]]: ValueEnumLike[Long, E] = macro EnumMacros.valueEnumLikeImpl[Long, E]
  implicit def kebsShortValueEnumScala[E <: ValueEnumLikeEntry[Short]]: ValueEnumLike[Short, E] = macro EnumMacros.valueEnumLikeImpl[Short, E]
  implicit def kebsByteValueEnumScala[E <: ValueEnumLikeEntry[Byte]]: ValueEnumLike[Byte, E] = macro EnumMacros.valueEnumLikeImpl[Byte, E]
  implicit def kebsCharValueEnumScala[E <: ValueEnumLikeEntry[Char]]: ValueEnumLike[Char, E] = macro EnumMacros.valueEnumLikeImpl[Char, E]
  implicit def kebsStringValueEnumScala[E <: ValueEnumLikeEntry[String]]: ValueEnumLike[String, E] = macro EnumMacros.valueEnumLikeImpl[String, E]
  // format: on
}

// an abort only skips the implicit candidate
final private[enums] class EnumMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with EnumMacrosImpl {
  import c.universe._

  def enumerationLikeImpl[E: c.WeakTypeTag]: c.Expr[EnumLike[E]] = deriveEnumLike[E](enumerationValues[E])

  def valueEnumLikeImpl[V: c.WeakTypeTag, E <: ValueEnumLikeEntry[V]: c.WeakTypeTag]: c.Expr[ValueEnumLike[V, E]] = {
    val entry = weakTypeOf[E].baseType(symbolOf[ValueEnumLikeEntry[_]])
    val value = entry.typeArgs.headOption.getOrElse(c.abort(c.enclosingPosition, s"${weakTypeOf[E]} is not a ValueEnumLikeEntry"))
    c.Expr[ValueEnumLike[V, E]](
      q"_root_.pl.iterators.kebs.core.enums.ValueEnumLike.apply[$value, ${weakTypeOf[E]}](${enumCases[E].tree})"
    )
  }

  // Obj.values, as in kebs 2: id order, inherited values, alias vals listed once
  private def enumerationValues[E: c.WeakTypeTag]: c.Expr[List[E]] = {
    @tailrec def dealiased(tpe: c.universe.Type): c.universe.Type = { val d = tpe.dealias; if (d eq tpe) tpe else dealiased(d) }
    dealiased(weakTypeOf[E]) match {
      case TypeRef(prefix, _, _) if prefix.typeSymbol.isModuleClass =>
        val enumeration = c.universe.internal.gen.mkAttributedQualifier(prefix)
        c.Expr[List[E]](q"$enumeration.values.toList.asInstanceOf[_root_.scala.List[${weakTypeOf[E]}]]")
      case _ => c.abort(c.enclosingPosition, s"${weakTypeOf[E]} is not a value of a scala.Enumeration object")
    }
  }
}
