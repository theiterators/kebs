package pl.iterators.kebs.core.support

import pl.iterators.kebs.core.macros.ValueClassLike

trait PartialOrderingSupport {

  implicit def partialOrderingFromValueClassLike[A, Rep](implicit
      vcLike: ValueClassLike[A, Rep],
      partialOrderingRep: PartialOrdering[Rep]
  ): PartialOrdering[A] =
    new PartialOrdering[A] {
      override def tryCompare(x: A, y: A): Option[Int] = partialOrderingRep.tryCompare(vcLike.unapply(x), vcLike.unapply(y))
      override def lteq(x: A, y: A): Boolean           = partialOrderingRep.lteq(vcLike.unapply(x), vcLike.unapply(y))
    }

}
