package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.scalacheck.macros.KebsScalacheckGeneratorsMacro

trait KebsScalacheckGenerators extends CommonArbitrarySupport {
  implicit def allGenerators[
      T,
      GeneratorParametersProviderT <: GeneratorParametersProvider,
      GeneratorsMinimalArbitrarySupportT <: GeneratorsMinimalArbitrarySupport,
      GeneratorsNormalArbitrarySupportT <: GeneratorsNormalArbitrarySupport,
      GeneratorsMaximalArbitrarySupportT <: GeneratorsMaximalArbitrarySupport
  ]: AllGenerators[T] = macro KebsScalacheckGeneratorsMacro.materializeGenerators[
    T,
    GeneratorParametersProviderT,
    GeneratorsMinimalArbitrarySupportT,
    GeneratorsNormalArbitrarySupportT,
    GeneratorsMaximalArbitrarySupportT
  ]
}
