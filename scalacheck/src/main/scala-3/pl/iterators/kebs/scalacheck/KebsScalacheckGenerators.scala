package pl.iterators.kebs.scalacheck

import org.scalacheck.Arbitrary
import scala.deriving.Mirror

trait KebsScalacheckGenerators extends CommonArbitrarySupport {
  inline implicit final def allGenerators[
      T,
      GeneratorParametersProviderT <: GeneratorParametersProvider,
      GeneratorsMinimalSupportT <: GeneratorsMinimalArbitrarySupport,
      GeneratorsNormalSupportT <: GeneratorsNormalArbitrarySupport,
      GeneratorsMaximalSupportT <: GeneratorsMaximalArbitrarySupport
  ](using
      inline m: Mirror.Of[T],
      generatorParams: GeneratorParametersProviderT,
      minimalSupport: GeneratorsMinimalSupportT,
      normalSupport: GeneratorsNormalSupportT,
      maximalSupport: GeneratorsMaximalSupportT
  ): AllGenerators[T] = {
    new AllGenerators[T] {

      override val minimal: Generator[T] = {
        import minimalSupport._
        val arbitrary = summon[Arbitrary[T]]
        new Generator[T] with GeneratorParametersProvider {
          def ArbT: Arbitrary[T] = arbitrary
          val params             = generatorParams.parameters
        }
      }
      override val maximal: Generator[T] = {
        import maximalSupport._
        val arbitrary = summon[Arbitrary[T]]
        new Generator[T] with GeneratorParametersProvider {
          def ArbT: Arbitrary[T] = arbitrary
          val params             = generatorParams.parameters
        }
      }
      override val normal: Generator[T] = {
        import normalSupport._
        val arbitrary = summon[Arbitrary[T]]
        new Generator[T] with GeneratorParametersProvider {
          def ArbT: Arbitrary[T] = arbitrary
          val params             = generatorParams.parameters
        }
      }
    }
  }
}
