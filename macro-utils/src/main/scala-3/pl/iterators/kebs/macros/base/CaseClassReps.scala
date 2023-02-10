package pl.iterators.kebs.macros.base

import scala.deriving.Mirror

final class CaseClass1Rep[CC, F1](val apply: F1 => CC, val unapply: CC => F1)

object CaseClass1Rep {
  inline given[T <: Product, F1](using m: Mirror.ProductOf[T], teq: m.MirroredElemTypes =:= F1 *: EmptyTuple.type): CaseClass1Rep[T, F1] =  {
    new CaseClass1Rep[T, F1](f1 => m.fromProduct(Tuple1(f1)), _.productElement(0).asInstanceOf[F1])
  }
}