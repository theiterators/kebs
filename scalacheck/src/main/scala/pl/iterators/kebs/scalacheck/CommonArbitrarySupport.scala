package pl.iterators.kebs.scalacheck

import org.scalacheck.Arbitrary
import pl.iterators.kebs.core.macros.ValueClassLike

trait CommonArbitrarySupport extends ScalacheckInstancesSupport {
  implicit def valueClassLikeArbitraryPredef[T, A](implicit
      rep: ValueClassLike[T, A],
      arbitrary: Arbitrary[A]
  ): Arbitrary[T] =
    Arbitrary(arbitrary.arbitrary.map(rep.apply(_)))
}

object CommonArbitrarySupport extends CommonArbitrarySupport
