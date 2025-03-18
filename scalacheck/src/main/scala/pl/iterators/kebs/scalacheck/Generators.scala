package pl.iterators.kebs.scalacheck

import org.scalacheck.rng.Seed
import org.scalacheck.{Arbitrary, Gen}

trait GeneratorParametersProvider {
  val parameters: Gen.Parameters = Gen.Parameters.default
}

trait DefaultGeneratorParametersProvider extends GeneratorParametersProvider {
  override val parameters: Gen.Parameters = Gen.Parameters.default
}

object DefaultGeneratorParametersProvider extends DefaultGeneratorParametersProvider

trait Generator[T] {
  p: GeneratorParametersProvider =>
  def ArbT: Arbitrary[T]

  def generate: T = ArbT.arbitrary.pureApply(parameters, Seed.random())
}

trait AllGenerators[T] {

  val normal: Generator[T]

  val minimal: Generator[T]

  val maximal: Generator[T]
}
