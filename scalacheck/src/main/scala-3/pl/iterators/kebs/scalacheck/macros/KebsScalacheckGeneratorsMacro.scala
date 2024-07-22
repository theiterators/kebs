package pl.iterators.kebs.scalacheck.macros

import pl.iterators.kebs.scalacheck._
import org.scalacheck.Arbitrary
import scala.deriving.Mirror
import io.github.martinhh.derived.scalacheck.deriveArbitrary
object KebsScalacheckGeneratorsMacro {

  inline implicit def materializeGenerators[T](using inline m: Mirror.Of[T]): AllGenerators[T] = {

      new AllGenerators[T] {

        trait GeneratorCreator
          extends CommonArbitrarySupport {

          def create: Generator[T]
        }
        
        object MinimalGeneratorCreator
          extends GeneratorCreator
          with MinimalArbitrarySupport {

          override def create = new Generator[T] {
            def ArbT: Arbitrary[T] = deriveArbitrary[T]
          }
        }

        object NormalGeneratorCreator
          extends GeneratorCreator {

          override def create = new Generator[T] {
            def ArbT: Arbitrary[T] = deriveArbitrary[T]
          }
        }

        object MaximalGeneratorCreator
          extends GeneratorCreator
          with MaximalArbitrarySupport {

          override def create = new Generator[T] {
            def ArbT: Arbitrary[T] = deriveArbitrary[T]
          }
        }

        override val minimal: Generator[T] = MinimalGeneratorCreator.create
        override val maximal: Generator[T] = MaximalGeneratorCreator.create
        override val normal: Generator[T] = NormalGeneratorCreator.create
      }
    }
  }
