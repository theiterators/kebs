package pl.iterators.kebs.support

import pl.iterators.kebs.macros.CaseClass1Rep

trait PartialOrderingSupport {

  implicit def partialOrderingFromCaseClass1Rep[A, Rep](implicit cc1Rep: CaseClass1Rep[A, Rep],
                                                        partialOrderingRep: PartialOrdering[Rep]): PartialOrdering[A] =
    new PartialOrdering[A] {
      override def tryCompare(x: A, y: A): Option[Int] = partialOrderingRep.tryCompare(cc1Rep.unapply(x), cc1Rep.unapply(y))
      override def lteq(x: A, y: A): Boolean           = partialOrderingRep.lteq(cc1Rep.unapply(x), cc1Rep.unapply(y))
    }

}
