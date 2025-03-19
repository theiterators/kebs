package pl.iterators.kebs

import org.scalacheck.rng.Seed
import org.scalacheck.{Arbitrary, Gen}

package object scalacheck {
  def generate[T](seed: Seed = Seed.random())(implicit arbitrary: Arbitrary[T], parameters: Gen.Parameters = Gen.Parameters.default): T = {
    arbitrary.arbitrary.pureApply(parameters, seed, retries = 1)
  }
}
