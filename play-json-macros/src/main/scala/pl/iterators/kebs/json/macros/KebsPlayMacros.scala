package pl.iterators.kebs.json.macros

import pl.iterators.kebs.macros.MacroUtils
import play.api.libs.json._

import scala.reflect.macros._

class KebsPlayMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  private val jsonReads            = typeOf[Reads[_]]
  private def jsonReadsOf(p: Type) = appliedType(jsonReads, p)

  private def materializeReads[A](A: Type, field: MethodSymbol): c.Expr[Reads[A]] = {
    val B          = field.typeSignatureIn(A).resultType
    val jsonReadsB = inferImplicitValue(jsonReadsOf(B), s"To materialize Reads for ${A.typeSymbol}, Reads[$B] is needed")
    val readsA     = q"$jsonReadsB.map(${apply(A)})"

    c.Expr[Reads[A]](readsA)
  }

  private val jsonWrites            = typeOf[Writes[_]]
  private def jsonWritesOf(p: Type) = appliedType(jsonWrites, p)

  private def materializeWrites[A](A: Type, field: MethodSymbol): c.Expr[Writes[A]] = {
    val B           = field.typeSignatureIn(A).resultType
    val jsonWritesB = inferImplicitValue(jsonWritesOf(B), s"To materialize Writes for ${A.typeSymbol}, Writes[$B] is needed")
    val writesA     = q"_root_.play.api.libs.json.Writes[$A]((obj: $A) => $jsonWritesB.writes(obj.$field))"

    c.Expr[Writes[A]](writesA)
  }

  def materializeFlatReads[T: c.WeakTypeTag]: c.Expr[Reads[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Reads, ${T.typeSymbol} must be a case class")

    T match {
      case Product1(_1) => materializeReads[T](T, _1)
      case _            => c.abort(c.enclosingPosition, "To materialize flat Reads, case class must have arity == 1")
    }
  }

  def materializeFlatWrites[T: c.WeakTypeTag](dummy: c.Tree): c.Expr[Writes[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Writes, ${T.typeSymbol} must be a case class")

    T match {
      case Product1(_1) => materializeWrites[T](T, _1)
      case _            => c.abort(c.enclosingPosition, "To materialize flat Writes, case class must have arity == 1")
    }
  }

}
