package pl.iterators.kebs.core.macros

import pl.iterators.kebs.core.macros.internal.ValueClassLikeMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[macros] trait CaseClass1ToValueClassCompat {
  implicit def repFromCaseClass[VC <: Product, F1]: ValueClassLike[VC, F1] = macro ValueClassRepMacros.materializeValueClassRep[VC, F1]
}

// whitebox: the macro's result type fixes F1, which the caller leaves for the compiler to infer
final private[macros] class ValueClassRepMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with ValueClassLikeMacrosImpl {
  def materializeValueClassRep[VC <: Product: c.WeakTypeTag, F1: c.WeakTypeTag]: c.Expr[ValueClassLike[VC, F1]] =
    c.Expr[ValueClassLike[VC, F1]](deriveValueClassLike[VC].value.tree)
}
