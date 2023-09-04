package pl.iterators.kebs.support

import pl.iterators.kebs.macros.ValueClassLike

trait EquivSupport {

  implicit def equivFromValueClassLike[A, Rep](implicit vcLike: ValueClassLike[A, Rep], equivRep: Equiv[Rep]): Equiv[A] =
    (x: A, y: A) => equivRep.equiv(vcLike.unapply(x), vcLike.unapply(y))

}
