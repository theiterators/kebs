package pl.iterators.kebs.core.support

import pl.iterators.kebs.core.macros.ValueClassLike

trait EquivSupport {

  implicit def equivFromValueClassLike[A, Rep](implicit vcLike: ValueClassLike[A, Rep], equivRep: Equiv[Rep]): Equiv[A] =
    (x: A, y: A) => equivRep.equiv(vcLike.unapply(x), vcLike.unapply(y))

}
