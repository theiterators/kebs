package pl.iterators.kebs.enums

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.blackbox

trait KebsEnum {
  implicit def enumScala2[E <: Enumeration#Value]: EnumLike[E] = macro EnumerationEntryMacros.enumOfImpl[E]
}

class EnumerationEntryMacros(val c: blackbox.Context) {
  import c.universe._

  def enumOfImpl[E <: Enumeration#Value : c.WeakTypeTag]: c.Expr[EnumLike[E]] = {
    import c.universe._
    val valueType = implicitly[c.WeakTypeTag[E]].tpe.dealias
    val objectStr = valueType.toString.replaceFirst(".Value$", "")
    val objectName = c.typecheck(c.parse(s"$objectStr: $objectStr.type"))
    c.Expr[EnumLike[E]](q"new _root_.pl.iterators.kebs.enums.EnumLike[$valueType] { override def values: immutable.Seq[${valueType}] = ($objectName).values.toSeq }")
  }
}

