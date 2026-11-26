package pl.iterators.kebs.core.macros

import pl.iterators.kebs.core.macros.internal.ValueClassLikeMacrosImpl

import scala.deriving.Mirror
import scala.quoted.*

private[macros] trait ValueClassLikeCompat {
  // selected via the Mirror, so the macro must not abort: a private/protected constructor or field is not accessible at
  // the call site, but the Mirror's fromProduct (generated in the companion) is
  inline implicit def repFromCaseClass[T <: Product, F1](using
      marked: ValueClass[T],
      m: Mirror.ProductOf[T],
      teq: m.MirroredElemTypes =:= F1 *: EmptyTuple.type
  ): ValueClassLike[T, F1] = ${ ValueClassLikeMacros.materialize[T, F1]('m) }

  // Scala 3 synthesises no Mirror for AnyVal case classes; transparent, so that a non-matching T skips this candidate and
  // the expansion's type fixes F1
  transparent inline implicit def repFromValueClass[T <: AnyVal, F1](using marked: ValueClass[T]): ValueClassLike[T, F1] =
    ${ ValueClassLikeMacros.materializeValueClass[T, F1] }
}

private[macros] trait ValueClassCompat {
  inline def derived[VC]: ValueClass[VC] = ${ ValueClassLikeMacros.marker[VC] }
}

final private[macros] class ValueClassLikeMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), ValueClassLikeMacrosImpl

private[macros] object ValueClassLikeMacros {
  def materialize[T <: Product: Type, F1: Type](m: Expr[Mirror.ProductOf[T]])(using q: Quotes): Expr[ValueClassLike[T, F1]] = {
    val macros = new ValueClassLikeMacros(q)
    macros.deriveValueClassLikeWithFallback[T, F1](
      // used inside another quote
      new macros.ProductFallback[T, F1](
        f => macros.withQuotes('{ $m.fromProduct(Tuple1($f)) }),
        t => macros.withQuotes('{ $t.productElement(0).asInstanceOf[F1] })
      )
    )
  }

  def materializeValueClass[T <: AnyVal: Type, F1: Type](using q: Quotes): Expr[ValueClassLike[T, F1]] =
    new ValueClassLikeMacros(q).deriveValueClassLike[T].value.asInstanceOf[Expr[ValueClassLike[T, F1]]]

  def marker[VC: Type](using q: Quotes): Expr[ValueClass[VC]] = {
    new ValueClassLikeMacros(q).checkValueClass[VC]
    '{ ValueClass.unchecked[VC] }
  }
}
