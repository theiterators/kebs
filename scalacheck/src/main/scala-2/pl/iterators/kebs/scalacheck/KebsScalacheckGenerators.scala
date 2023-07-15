
import pl.iterators.kebs.scalacheck.macros.KebsScalacheckGeneratorsMacro
import .AllGenerators
import .KebsScalacheckGeneratorsMacro

trait KebsScalacheckGenerators {
  implicit def allGenerators[T]: pl.iterators.kebs.scalacheck.AllGenerators[T] =
    macro KebsScalacheckGeneratorsMacro.materializeGenerators[T]
}
