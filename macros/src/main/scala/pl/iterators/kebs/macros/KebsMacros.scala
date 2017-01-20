package pl.iterators.kebs.macros

import scala.reflect.macros._
import slick.lifted.{Isomorphism, MappedProjection}

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
    val to   = caseAccessor.typeSignatureIn(from).resultType

    /*
      One special case to check: if we're generating mapped projection of one-column table then we'll end up with conflicting implicits:
      - mappedProjectionShape from user-written '<>'
      - repColumnShape from type mapper kebs has generated (BaseTypedType)
      Therefore we check if the macro is being expanded from `ProvenShape#proveShapeOf[MappedProjection[CC,B], CC]` call site. If it is,
      then we abort implicit generation.
      See "Slick mapping - one element wrapper" and "Slick mapping - matryoshka case {1,2}" test cases.
     */
    assertNoEnclosingMappedProjection(from, to)

    val map   = q"_.$caseAccessor"
    val comap = q"${companion(from)}(_)"

    q"new _root_.slick.lifted.Isomorphism[$from, $to]($map, $comap)"
  }

  private def assertNoEnclosingMappedProjection(caseClassType: Type, to: Type) = {
    val MappedProjection = appliedType(typeOf[MappedProjection[_, _]], caseClassType, to)
    val mappedProjection = MappedProjection.typeSymbol
    val cc               = caseClassType.typeSymbol

    val inMappedProjectionContext = c.enclosingImplicits.exists(_.tree match {
      case proveShapeOf(_T, _U) => _T == mappedProjection && _U == cc
      case _                    => false
    })
    if (inMappedProjectionContext) {
      val msg = s"Aborted type-mapper generation for $cc because it seems you wrote an explicit $MappedProjection for this type"
      c.warning(c.enclosingPosition, msg)
      c.abort(c.enclosingPosition, msg)
    }
  }

  object proveShapeOf {
    def unapply(tree: Tree): Option[(Symbol, Symbol)] = tree match {
      case q"lifted.this.ProvenShape.proveShapeOf[..$ts]($_)" =>
        ts match {
          case t :: u :: Nil => Some((t.tpe.typeSymbol, u.tpe.typeSymbol))
          case _             => None
        }
      case _ => None
    }
  }
}
