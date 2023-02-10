package pl.iterators.kebs.support

import pl.iterators.kebs.macros.base.CaseClass1Rep

trait EquivSupport {

  implicit def equivFromCaseClass1Rep[A, Rep](implicit cc1Rep: CaseClass1Rep[A, Rep], equivRep: Equiv[Rep]): Equiv[A] =
    (x: A, y: A) => equivRep.equiv(cc1Rep.unapply(x), cc1Rep.unapply(y))

}
