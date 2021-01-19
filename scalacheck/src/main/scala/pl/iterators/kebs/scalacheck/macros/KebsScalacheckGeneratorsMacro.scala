package pl.iterators.kebs.scalacheck.macros

import pl.iterators.kebs.macros.MacroUtils
import scala.language.experimental.macros
import scala.reflect.macros._
import pl.iterators.kebs.scalacheck._

class KebsScalacheckGeneratorsMacro(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  final def materializeGenerators[T: c.WeakTypeTag]: c.Expr[AllGenerators[T]] = {
    val T = weakTypeOf[T]
    //assertCaseClass(T, s"To materialize json.Schema[T], ${T.typeSymbol} must be a case class")
    val tree =
      q"""{
         new pl.iterators.kebs.scalacheck.AllGenerators[$T] {
            import pl.iterators.kebs.scalacheck._
            import org.scalacheck.Arbitrary
            import org.scalacheck.ScalacheckShapeless._
            import enumeratum.scalacheck._

            override val minimal: MinimalGenerator[$T] = new MinimalGenerator[$T] {
              def ArbT = implicitly[Arbitrary[$T]]
            }

            override val maximal: MaximalGenerator[$T] = new MaximalGenerator[$T] {
              def ArbT = implicitly[Arbitrary[$T]]
            }

            override val normal: Generator[$T] = new Generator[$T] {
              def ArbT = implicitly[Arbitrary[$T]]
            }
          }
         }"""
    c.Expr[AllGenerators[T]](tree)
  }
}
