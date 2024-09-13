package pl.iterators.kebs.scalacheck

import org.scalacheck.rng.Seed
import org.scalacheck.{Arbitrary, Gen}

trait Generator[T] extends CommonArbitrarySupport {
  def ArbT: Arbitrary[T]

  def generate: T = ArbT.arbitrary.pureApply(Gen.Parameters.default, Seed.random())
}

trait AllGenerators[T] {

  val normal: Generator[T]

  val minimal: Generator[T]

  val maximal: Generator[T]
}
