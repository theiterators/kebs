package pl.iterators.kebs.scalacheck.macros

import pl.iterators.kebs.core.macros.MacroUtils
import pl.iterators.kebs.scalacheck._

import scala.reflect.macros._

class KebsScalacheckGeneratorsMacro(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  final def materializeGenerators[
      T: c.WeakTypeTag,
      GeneratorParametersProviderT <: GeneratorParametersProvider: c.WeakTypeTag,
      GeneratorsMinimalArbitrarySupportT <: GeneratorsMinimalArbitrarySupport: c.WeakTypeTag,
      GeneratorsNormalArbitrarySupportT <: GeneratorsNormalArbitrarySupport: c.WeakTypeTag,
      GeneratorsMaximalArbitrarySupportT <: GeneratorsMaximalArbitrarySupport: c.WeakTypeTag
  ]: c.Expr[AllGenerators[T]] = {
    val T = weakTypeOf[T]
    val tree =
      q"""{
         new _root_.pl.iterators.kebs.scalacheck.AllGenerators[$T] {

            trait GeneratorCreator
              extends _root_.pl.iterators.kebs.scalacheck.CommonArbitrarySupport {

              def create: _root_.pl.iterators.kebs.scalacheck.Generator[$T]
            }
            
            object MinimalGeneratorCreator
              extends GeneratorCreator
              with ${weakTypeOf[GeneratorsMinimalArbitrarySupportT]} {

              override def create: _root_.pl.iterators.kebs.scalacheck.Generator[$T] =
                new _root_.pl.iterators.kebs.scalacheck.Generator[$T]
                  with ${weakTypeOf[GeneratorParametersProviderT]} {
                def ArbT = implicitly[_root_.org.scalacheck.Arbitrary[$T]]
              }
            }
        
            object NormalGeneratorCreator
              extends GeneratorCreator
              with ${weakTypeOf[GeneratorsNormalArbitrarySupportT]} {

              override def create: _root_.pl.iterators.kebs.scalacheck.Generator[$T] =
                new _root_.pl.iterators.kebs.scalacheck.Generator[$T]
                  with ${weakTypeOf[GeneratorParametersProviderT]} {
                def ArbT = implicitly[_root_.org.scalacheck.Arbitrary[$T]]
              }
            }
        
            object MaximalGeneratorCreator
              extends GeneratorCreator
              with ${weakTypeOf[GeneratorsMaximalArbitrarySupportT]} {

              override def create: _root_.pl.iterators.kebs.scalacheck.Generator[$T] =
                new _root_.pl.iterators.kebs.scalacheck.Generator[$T]
                  with ${weakTypeOf[GeneratorParametersProviderT]} {
                def ArbT = implicitly[_root_.org.scalacheck.Arbitrary[$T]]
              }
            }
            
            override lazy val minimal: _root_.pl.iterators.kebs.scalacheck.Generator[$T] = MinimalGeneratorCreator.create
            override lazy val maximal: _root_.pl.iterators.kebs.scalacheck.Generator[$T] = MaximalGeneratorCreator.create
            override lazy val normal: _root_.pl.iterators.kebs.scalacheck.Generator[$T] = NormalGeneratorCreator.create
          }
         }"""
    c.Expr[AllGenerators[T]](tree)
  }
}
