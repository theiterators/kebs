package pl.iterators.kebs.support

import pl.iterators.kebs.macros.ValueClassLike

trait EquivSupport {

  implicit def equivFromCaseClass1Rep[A, Rep](implicit cc1Rep: ValueClassLike[A, Rep], equivRep: Equiv[Rep]): Equiv[A] =
    (x: A, y: A) => equivRep.equiv(cc1Rep.unapply(x), cc1Rep.unapply(y))

}
