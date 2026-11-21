package pl.iterators.kebs.core.macros.internal

import scala.deriving.Mirror
import scala.quoted.*

// listed through the Mirror, as in kebs 2: hearth 0.4.5 mistypes cases declared in a trait mixed into an object
private[kebs] object MirrorCases {
  def of[E: Type](using Quotes): Option[Expr[List[E]]] =
    Expr.summon[Mirror.SumOf[E]].flatMap(casesOf).map(cases => Expr.ofList(cases.map(c => '{ $c.asInstanceOf[E] })))

  private def casesOf(mirror: Expr[Mirror.Sum])(using Quotes): Option[List[Expr[Any]]] =
    mirror match {
      case '{ $m: Mirror.Sum { type MirroredElemTypes = elems } } => elemCases[elems]
      case _                                                      => None
    }

  private def elemCases[T: Type](using Quotes): Option[List[Expr[Any]]] =
    summon[Type[T]] match {
      case '[EmptyTuple] => Some(Nil)
      case '[h *: t]     =>
        val head = Expr.summon[ValueOf[h]] match {
          case Some(valueOf) => Some(List('{ $valueOf.value }))
          case None          => Expr.summon[Mirror.SumOf[h]].flatMap(casesOf)
        }
        for { cases <- head; rest <- elemCases[t] } yield cases ++ rest
      case _ => None
    }
}
