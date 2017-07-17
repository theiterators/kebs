package pl.iterators.kebs.avro.macros

import com.sksamuel.avro4s._
import pl.iterators.kebs.macros.MacroUtils

import scala.reflect.macros.whitebox

class AvroKebsMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  private val toSchema  = typeOf[ToSchema[_]]
  private val toValue   = typeOf[ToValue[_]]
  private val fromValue = typeOf[FromValue[_]]

  private def wrapToSchema(A: Type, as: Type) = {
    val asToSchema = appliedType(toSchema, as)
    val delegate   = inferImplicitValue(asToSchema, s"To materialize ToSchema for ${A.typeSymbol}, ToSchema[$as] is needed")

    q"${_this}.wrapToSchema[$A]($delegate)"
  }

  private def wrapToValue(caseClass: Type, field: MethodSymbol) = {
    val as        = resultType(field, caseClass)
    val asToValue = appliedType(toValue, as)
    val delegate  = inferImplicitValue(asToValue, s"To materialize ToValue for ${caseClass.typeSymbol}, ToValue[$as] is needed")

    q"${_this}.wrapToValue[$caseClass, $as](_.$field, $delegate)"
  }

  private def wrapFromValue(caseClass: Type, valueType: Type) = {
    val fromValueType = appliedType(fromValue, valueType)
    val delegate =
      inferImplicitValue(fromValueType, s"To materialize FromValue for ${caseClass.typeSymbol}, FromValue[$valueType] is needed")

    q"${_this}.wrapFromValue[$caseClass, $valueType](${apply(caseClass)}, $delegate)"
  }

  def materializeToSchema[A: c.WeakTypeTag]: c.Expr[ToSchema[A]] = {
    val A = weakTypeOf[A]
    assertCaseClass(A, s"To materialize ToSchema, ${A.typeSymbol} must be a case class")
    assertAnyVal(A, s"To materialize ToSchema, ${A.typeSymbol} must inherit from AnyVal")

    A match {
      case Product1(_1) => c.Expr[ToSchema[A]](wrapToSchema(A, resultType(_1, A)))
      case _            => c.abort(c.enclosingPosition, "To materialize ToSchema, case class must have arity == 1")
    }

  }

  def materializeToValue[A: c.WeakTypeTag]: c.Expr[ToValue[A]] = {
    val A = weakTypeOf[A]
    assertCaseClass(A, s"To materialize ToValue, ${A.typeSymbol} must be a case class")
    assertAnyVal(A, s"To materialize ToValue, ${A.typeSymbol} must inherit from AnyVal")

    A match {
      case Product1(_1) => c.Expr[ToValue[A]](wrapToValue(A, _1))
      case _            => c.abort(c.enclosingPosition, "To materialize ToValue, case class must have arity == 1")
    }

  }

  def materializeFromValue[A: c.WeakTypeTag]: c.Expr[FromValue[A]] = {
    val A = weakTypeOf[A]
    assertCaseClass(A, s"To materialize FromValue, ${A.typeSymbol} must be a case class")
    assertAnyVal(A, s"To materialize FromValue, ${A.typeSymbol} must inherit from AnyVal")

    A match {
      case Product1(_1) => c.Expr[FromValue[A]](wrapFromValue(A, resultType(_1, A)))
      case _            => c.abort(c.enclosingPosition, "To materialize FromValue, case class must have arity == 1")
    }

  }
}
