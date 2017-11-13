package pl.iterators.kebs.macros

import scala.reflect.macros._
import slick.lifted.Isomorphism

class KebsMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  def materializeValueColumn[CC <: Product: c.WeakTypeTag, B: c.WeakTypeTag]: c.Expr[Isomorphism[CC, B]] = {
    val CaseClass = weakTypeOf[CC]
    assertCaseClass(CaseClass, s"To materialize column type, ${CaseClass.typeSymbol} must be a case class")

    CaseClass match {
      case Product1(_1) => c.Expr[Isomorphism[CC, B]](materializeIsomorphism(CaseClass, _1))
      case _            => c.abort(c.enclosingPosition, "To materialize column type, case class must have arity == 1")
    }
  }

  private def materializeIsomorphism(caseClassType: Type, caseAccessor: MethodSymbol) = {
    val from = caseClassType
    val to   = resultType(caseAccessor, caseClassType)

    val map   = q"_.$caseAccessor"
    val comap = apply(from)

    q"new _root_.slick.lifted.Isomorphism[$from, $to]($map, $comap)"
  }
}
