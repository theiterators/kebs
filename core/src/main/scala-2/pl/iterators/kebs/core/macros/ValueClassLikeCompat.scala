package pl.iterators.kebs.core.macros

import pl.iterators.kebs.core.macros.internal.ValueClassLikeMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[macros] trait ValueClassLikeCompat {
  implicit def repFromCaseClass[T <: Product, F](implicit m: ValueClass[T]): ValueClassLike[T, F] = macro ValueClassRepMacros.rep[T, F]
}

private[macros] trait ValueClassCompat {
  def derived[VC]: ValueClass[VC] = macro ValueClassRepMacros.marker[VC]
}

// whitebox, so that the expansion's type fixes F1 left open by the caller
final private[macros] class ValueClassRepMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with ValueClassLikeMacrosImpl {
  def rep[VC <: Product: c.WeakTypeTag, F1: c.WeakTypeTag](
      m: c.Expr[ValueClass[VC]]
  ): c.Expr[ValueClassLike[VC, F1]] =
    c.Expr[ValueClassLike[VC, F1]](deriveValueClassLike[VC].value.tree)

  def marker[VC: c.WeakTypeTag]: c.Expr[ValueClass[VC]] = {
    checkValueClass[VC]
    c.Expr[ValueClass[VC]](c.universe.reify(ValueClass.unchecked[VC]).tree)
  }
}
