package pl.iterators.kebs.scalacheck

import org.scalacheck.Arbitrary
import scala.deriving.Mirror

trait KebsScalacheckGenerators extends CommonArbitrarySupport {
  inline implicit final def allGenerators[T](using inline m: Mirror.Of[T]): AllGenerators[T] = {
    new AllGenerators[T] {

      override val minimal: Generator[T] = {
        import MinimalArbitrarySupport._
        val arbitrary = summon[Arbitrary[T]]
        new Generator[T] {
          def ArbT: Arbitrary[T] = arbitrary
        }
      }
      override val maximal: Generator[T] = {
        import MaximalArbitrarySupport._
        val arbitrary = summon[Arbitrary[T]]
        new Generator[T] {
          def ArbT: Arbitrary[T] = arbitrary
        }
      }
      override val normal: Generator[T] = {
        val arbitrary = summon[Arbitrary[T]]
        new Generator[T] {
          def ArbT: Arbitrary[T] = arbitrary
        }
      }
    }
  }
}
