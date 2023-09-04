package pl.iterators.kebs.macros

import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.macros.whitebox

final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1)

object ValueClassLike {
  implicit def repFromCaseClass[VC <: Product, F1]: ValueClassLike[VC, F1] = macro ValueClassRepMacros.materializeValueClassRep[VC, F1]
}

class ValueClassRepMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  def materializeValueClassRep[VC <: Product: c.WeakTypeTag, F1: c.WeakTypeTag]: c.Expr[ValueClassLike[VC, F1]] = {
    val ValueClass = weakTypeOf[VC]
    assertCaseClass(ValueClass, s"To materialize value class representation, ${ValueClass.typeSymbol} must be a value class")

    ValueClass match {
      case Product1(_1) => c.Expr[ValueClassLike[VC, F1]](materializeRep1(ValueClass, _1))
      case _            => c.abort(c.enclosingPosition, "To materialize ValueClassLike, case class must have arity == 1")
    }
  }

  private def materializeRep1(caseClassType: Type, caseAccessor: MethodSymbol) = {
    val f1 = resultType(caseAccessor, caseClassType)

    val unapplyF = q"_.$caseAccessor"
    val applyF   = apply(caseClassType)

    q"new _root_.pl.iterators.kebs.macros.ValueClassLike[$caseClassType, $f1]($applyF, $unapplyF)"
  }
}
