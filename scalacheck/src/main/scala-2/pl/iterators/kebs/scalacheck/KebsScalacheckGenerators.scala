package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.scalacheck.macros.KebsScalacheckGeneratorsMacro

trait KebsScalacheckGenerators {
  // format: off
  implicit def allGenerators[T]: pl.iterators.kebs.scalacheck.AllGenerators[T] =
    macro KebsScalacheckGeneratorsMacro.materializeGenerators[T]
  // format: on
}
