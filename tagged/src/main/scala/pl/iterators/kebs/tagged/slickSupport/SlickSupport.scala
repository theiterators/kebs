package pl.iterators.kebs.tagged.slickSupport

import pl.iterators.kebs.macros.MacroUtils
import pl.iterators.kebs.tagged._
import slick.lifted.Isomorphism

import scala.reflect.macros.blackbox

trait SlickSupportLowPriority {
  implicit def taggedColumnTypeAlias[T, U]: Isomorphism[T @@ U, T] = macro SlickSupportMacros.taggedColumnType[T, U]
}

trait SlickSupport extends SlickSupportLowPriority {
  implicit def taggedColumnType[T, U]: Isomorphism[T @@ U, T] = new Isomorphism[T @@ U, T](identity, _.@@[U])
}

class SlickSupportMacros(override val c: blackbox.Context) extends MacroUtils {
  def taggedColumnType[T: c.WeakTypeTag, U: c.WeakTypeTag]: c.Expr[Isomorphism[T @@ U, T]] = {
    import c.universe._

    val T = weakTypeOf[T]
    val U = weakTypeOf[U]

    c.Expr[Isomorphism[T @@ U, T]](q"${_this}.taggedColumnType[$T, $U]")
  }
}

object SlickSupport extends SlickSupport
