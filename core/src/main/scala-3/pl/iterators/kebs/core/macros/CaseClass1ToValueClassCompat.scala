package pl.iterators.kebs.core.macros

import pl.iterators.kebs.core.macros.internal.ValueClassLikeMacrosImpl

import scala.deriving.Mirror
import scala.quoted.*

private[macros] trait CaseClass1ToValueClassCompat {
  // selected via the Mirror, so the macro must not abort: a private/protected constructor or field is not accessible at
  // the call site, but the Mirror's fromProduct (generated in the companion) is
  inline implicit def repFromCaseClass[T <: Product, F1](using
      m: Mirror.ProductOf[T],
      teq: m.MirroredElemTypes =:= F1 *: EmptyTuple.type
  ): ValueClassLike[T, F1] = ${ ValueClassLikeMacros.materialize[T, F1]('m) }
}

final private[macros] class ValueClassLikeMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), ValueClassLikeMacrosImpl

private[macros] object ValueClassLikeMacros {
  def materialize[T <: Product: Type, F1: Type](m: Expr[Mirror.ProductOf[T]])(using q: Quotes): Expr[ValueClassLike[T, F1]] = {
    val macros = new ValueClassLikeMacros(q)
    macros.deriveValueClassLikeWithFallback[T, F1](
      new macros.ProductFallback[T, F1](
        f => '{ $m.fromProduct(Tuple1($f)) },
        t => '{ $t.productElement(0).asInstanceOf[F1] }
      )
    )
  }
}
