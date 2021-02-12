package pl.iterators.kebs.scalacheck.macros

import pl.iterators.kebs.macros.MacroUtils
import scala.language.experimental.macros
import scala.reflect.macros._
import pl.iterators.kebs.scalacheck._

class KebsScalacheckGeneratorsMacro(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  final def materializeGenerators[T: c.WeakTypeTag]: c.Expr[AllGenerators[T]] = {
    val T = weakTypeOf[T]
    val tree =
      q"""{
         new pl.iterators.kebs.scalacheck.AllGenerators[$T] {
            import pl.iterators.kebs.scalacheck._
            import org.scalacheck.Arbitrary

            trait GeneratorCreator extends CommonArbitrarySupport {
              def create: Generator[$T]
            }
            
            object MinimalGeneratorCreator extends GeneratorCreator with MinimalArbitrarySupport {
              override def create = new Generator[$T] {
                def ArbT = implicitly[Arbitrary[$T]]
              }
            }
        
            object NormalGeneratorCreator extends GeneratorCreator {
              override def create = new Generator[$T] {
                def ArbT = implicitly[Arbitrary[$T]]
              }
            }
        
            object MaximalGeneratorCreator extends GeneratorCreator with MaximalArbitrarySupport {
              override def create = new Generator[$T] {
                def ArbT = implicitly[Arbitrary[$T]]
              }
            }
            
            override val minimal: Generator[$T] = MinimalGeneratorCreator.create
            override val maximal: Generator[$T] = MaximalGeneratorCreator.create
            override val normal: Generator[$T] = NormalGeneratorCreator.create
          }
         }"""
    c.Expr[AllGenerators[T]](tree)
  }
}
