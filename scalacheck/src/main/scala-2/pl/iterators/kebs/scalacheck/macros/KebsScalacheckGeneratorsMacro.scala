
import pl.iterators.kebs.macros.MacroUtils
import scala.language.experimental.macros
import scala.reflect.macros._
import pl.iterators.kebs.scalacheck._
import .AllGenerators

class KebsScalacheckGeneratorsMacro(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  final def materializeGenerators[T: c.WeakTypeTag]: c.Expr[AllGenerators[T]] = {
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
              with _root_.pl.iterators.kebs.scalacheck.MinimalArbitrarySupport {

              override def create = new _root_.pl.iterators.kebs.scalacheck.Generator[$T] {
                def ArbT = implicitly[_root_.org.scalacheck.Arbitrary[$T]]
              }
            }
        
            object NormalGeneratorCreator
              extends GeneratorCreator {

              override def create = new _root_.pl.iterators.kebs.scalacheck.Generator[$T] {
                def ArbT = implicitly[_root_.org.scalacheck.Arbitrary[$T]]
              }
            }
        
            object MaximalGeneratorCreator
              extends GeneratorCreator
              with _root_.pl.iterators.kebs.scalacheck.MaximalArbitrarySupport {

              override def create = new _root_.pl.iterators.kebs.scalacheck.Generator[$T] {
                def ArbT = implicitly[_root_.org.scalacheck.Arbitrary[$T]]
              }
            }
            
            override val minimal: _root_.pl.iterators.kebs.scalacheck.Generator[$T] = MinimalGeneratorCreator.create
            override val maximal: _root_.pl.iterators.kebs.scalacheck.Generator[$T] = MaximalGeneratorCreator.create
            override val normal: _root_.pl.iterators.kebs.scalacheck.Generator[$T] = NormalGeneratorCreator.create
          }
         }"""
    c.Expr[AllGenerators[T]](tree)
  }
}
