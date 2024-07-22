package pl.iterators.kebs.core.macros

import scala.deriving.Mirror

final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1)

trait CaseClass1ToValueClass {
  implicit def repFromCaseClass[T <: Product, F1](using m: Mirror.ProductOf[T], teq: m.MirroredElemTypes =:= F1 *: EmptyTuple.type): ValueClassLike[T, F1] =  {
    new ValueClassLike[T, F1](f1 => m.fromProduct(Tuple1(f1)), _.productElement(0).asInstanceOf[F1])
  }
}
object CaseClass1ToValueClass extends CaseClass1ToValueClass