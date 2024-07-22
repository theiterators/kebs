package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.scalacheck.macros.KebsScalacheckGeneratorsMacro
import scala.deriving.Mirror

trait KebsScalacheckGenerators {
   inline implicit final def allGenerators[T](using inline m: Mirror.Of[T]): AllGenerators[T] = KebsScalacheckGeneratorsMacro.materializeGenerators[T]
}
