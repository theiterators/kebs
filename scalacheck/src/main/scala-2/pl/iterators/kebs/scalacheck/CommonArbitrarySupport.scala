package pl.iterators.kebs.scalacheck

import enumeratum.ScalacheckInstances
import org.scalacheck.Arbitrary
import pl.iterators.kebs.core.macros.ValueClassLike

trait CommonArbitrarySupport extends ScalacheckInstances with ScalacheckInstancesSupport {
  implicit def valueClassLikeArbitraryPredef[T, A](
                                                   implicit rep: ValueClassLike[T, A],
                                                   arbitrary: Arbitrary[A]
  ): Arbitrary[T] =
    Arbitrary(arbitrary.arbitrary.map(rep.apply(_)))
}