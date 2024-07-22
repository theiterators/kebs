package pl.iterators.kebs.scalacheck

import org.scalacheck.Arbitrary
import pl.iterators.kebs.core.macros.ValueClassLike

import enumeratum.ScalacheckInstances

trait CommonArbitrarySupport extends ScalacheckInstances {
  implicit def ValueClassLikeArbitraryPredef[T, A](implicit
      rep: ValueClassLike[T, A],
      arbitrary: Arbitrary[A]
  ): Arbitrary[T] =
    Arbitrary(arbitrary.arbitrary.map(rep.apply(_)))
}
