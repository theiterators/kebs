package pl.iterators.kebs.macros

import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.macros.whitebox

final class CaseClass1Rep[CC, F1](val apply: F1 => CC, val unapply: CC => F1)

object CaseClass1Rep {
  implicit def repFromCaseClass[CC <: Product, F1]: CaseClass1Rep[CC, F1] = macro CaseClassRepMacros.materializeCaseClass1Rep[CC, F1]
}

class CaseClassRepMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  def materializeCaseClass1Rep[CC <: Product: c.WeakTypeTag, F1: c.WeakTypeTag]: c.Expr[CaseClass1Rep[CC, F1]] = {
    val CaseClass = weakTypeOf[CC]
    assertCaseClass(CaseClass, s"To materialize case class representation, ${CaseClass.typeSymbol} must be a case class")

    CaseClass match {
      case Product1(_1) => c.Expr[CaseClass1Rep[CC, F1]](materializeRep1(CaseClass, _1))
      case _            => c.abort(c.enclosingPosition, "To materialize CaseClass1Rep, case class must have arity == 1")
    }
  }

  private def materializeRep1(caseClassType: Type, caseAccessor: MethodSymbol) = {
    val f1 = resultType(caseAccessor, caseClassType)

    val unapplyF = q"_.$caseAccessor"
    val applyF   = apply(caseClassType)

    q"new _root_.pl.iterators.kebs.macros.CaseClass1Rep[$caseClassType, $f1]($applyF, $unapplyF)"
  }
}
