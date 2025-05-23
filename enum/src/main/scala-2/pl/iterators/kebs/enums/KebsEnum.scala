package pl.iterators.kebs.enums

import pl.iterators.kebs.core.enums.EnumLike

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait KebsEnum {
  implicit def enumScala[E <: Enumeration#Value]: EnumLike[E] = macro EnumerationEntryMacros.enumOfImpl[E]
}

class EnumerationEntryMacros(val c: blackbox.Context) {
  def enumOfImpl[E <: Enumeration#Value: c.WeakTypeTag]: c.Expr[EnumLike[E]] = {
    import c.universe._
    val valueType  = implicitly[c.WeakTypeTag[E]].tpe.dealias
    val objectStr  = valueType.toString.replaceFirst(".Value$", "")
    val objectName = c.typecheck(c.parse(s"$objectStr: $objectStr.type"))
    c.Expr[EnumLike[E]](
      q"new _root_.pl.iterators.kebs.core.enums.EnumLike[$valueType] { override def valuesToNamesMap: Map[$valueType, String] = ($objectName).values.map(v => v -> v.toString).toMap }"
    )
  }
}
