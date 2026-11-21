package pl.iterators.kebs.core.macros

import pl.iterators.kebs.core.macros.internal.ValueClassLikeMacrosImpl

import scala.deriving.Mirror
import scala.quoted.*

private[macros] trait CaseClass1ToValueClassCompat {
  // The Mirror evidence infers F1 and keeps types that are not single-field products from reaching the macro
  // (a non-transparent inline given is expanded after it has been selected, so a macro failure would be a hard error).
  inline implicit def repFromCaseClass[T <: Product, F1](using
      m: Mirror.ProductOf[T],
      teq: m.MirroredElemTypes =:= F1 *: EmptyTuple.type
  ): ValueClassLike[T, F1] = ${ ValueClassLikeMacros.materialize[T, F1] }
}

final private[macros] class ValueClassLikeMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), ValueClassLikeMacrosImpl

private[macros] object ValueClassLikeMacros {
  def materialize[T: Type, F1: Type](using q: Quotes): Expr[ValueClassLike[T, F1]] =
    new ValueClassLikeMacros(q).deriveValueClassLike[T].value.asInstanceOf[Expr[ValueClassLike[T, F1]]]
}
