package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.scalacheck.macros.KebsScalacheckGeneratorsMacro
import scala.quoted.Quotes
import scala.deriving.Mirror
import pl.iterators.kebs.scalacheck.AllGenerators
trait KebsScalacheckGenerators {
   inline implicit final def allGenerators[T](using inline m: Mirror.Of[T]): AllGenerators[T] = KebsScalacheckGeneratorsMacro.materializeGenerators[T]
}
