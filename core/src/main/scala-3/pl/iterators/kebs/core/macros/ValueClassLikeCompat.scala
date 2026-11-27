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
  def materialize[T <: Product: Type, F1: Type](m: Expr[Mirror.ProductOf[T]])(using q: Quotes): Expr[ValueClassLike[T, F1]] =
    plain[T].filter(_.isExprOf[ValueClassLike[T, F1]]).map(_.asExprOf[ValueClassLike[T, F1]]).getOrElse(viaHearth[T, F1](m))

  private def viaHearth[T <: Product: Type, F1: Type](m: Expr[Mirror.ProductOf[T]])(using q: Quotes): Expr[ValueClassLike[T, F1]] = {
    val macros = new ValueClassLikeMacros(q)
    macros.deriveValueClassLikeWithFallback[T, F1](
      // used inside another quote
      new macros.ProductFallback[T, F1](
        f => macros.withQuotes('{ $m.fromProduct(Tuple1($f)) }),
        t => macros.withQuotes('{ $t.productElement(0).asInstanceOf[F1] })
      )
    )
  }

  // tried in every search for a ValueClassLike of an AnyVal, primitives included, so it gives up before hearth's set-up
  def materializeValueClass[T <: AnyVal: Type, F1: Type](using q: Quotes): Expr[ValueClassLike[T, F1]] = {
    import q.reflect.*
    val symbol = TypeRepr.of[T].dealias.typeSymbol
    if (!symbol.isClassDef || !symbol.flags.is(Flags.Case) || symbol.flags.is(Flags.Module))
      report.errorAndAbort(s"${symbol.name} is not a case class") // Type.show would cost more than the rest
    plain[T].getOrElse(new ValueClassLikeMacros(q).deriveValueClassLike[T].value).asInstanceOf[Expr[ValueClassLike[T, F1]]]
  }

  // The usual case without hearth, whose set-up costs more than the rest of an expansion, made at each search: a public case
  // class with a public field and no apply of its own, built with its constructor as its synthetic apply would be.
  private def plain[T: Type](using q: Quotes): Option[Expr[ValueClassLike[T, ?]]] = {
    import q.reflect.*
    def isPublic(symbol: Symbol): Boolean =
      !symbol.flags.is(Flags.Private) && !symbol.flags.is(Flags.Protected) && symbol.privateWithin.isEmpty &&
        symbol.protectedWithin.isEmpty
    val tpe         = TypeRepr.of[T].dealias
    val cls         = tpe.typeSymbol
    val constructor = cls.primaryConstructor
    val parameters  = constructor.paramSymss.filterNot(_.headOption.exists(_.isType)).flatten
    val ownApply    = cls.companionModule.methodMember("apply").exists(!_.flags.is(Flags.Synthetic))
    (parameters, cls.caseFields) match {
      case (List(_), List(field)) if cls.flags.is(Flags.Case) && field.isValDef && isPublic(constructor) && isPublic(field) && !ownApply =>
        tpe.memberType(field).asType match {
          case '[f] =>
            def construct(value: Expr[f]): Expr[T] =
              New(Inferred(tpe)).select(constructor).appliedToTypes(tpe.typeArgs).appliedTo(value.asTerm).asExprOf[T]
            def access(obj: Expr[T]): Expr[f] = obj.asTerm.select(field).asExprOf[f]
            Some('{ new ValueClassLike[T, f]((value: f) => ${ construct('value) }, (obj: T) => ${ access('obj) }) })
        }
      case _ => None
    }
  }

  def marker[VC: Type](using q: Quotes): Expr[ValueClass[VC]] = {
    new ValueClassLikeMacros(q).checkValueClass[VC]
    '{ ValueClass.unchecked[VC] }
  }
}
