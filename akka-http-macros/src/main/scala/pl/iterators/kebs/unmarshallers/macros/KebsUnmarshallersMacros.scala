package pl.iterators.kebs.unmarshallers.macros

import akka.http.scaladsl.unmarshalling.Unmarshaller
import pl.iterators.kebs.macros.MacroUtils

import scala.reflect.macros.whitebox

class KebsUnmarshallersMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  def materializeUnmarshaller[A: c.WeakTypeTag, B <: Product: c.WeakTypeTag](dummy: c.Tree): c.Expr[Unmarshaller[A, B]] = {
    val B = weakTypeOf[B]
    assertCaseClass(B, s"To materialize unmarshaller, ${B.typeSymbol} must be a case class")

    caseAccessors(B) match {
      case Nil => c.Expr[Unmarshaller[Any, B]](materializeAny(B))
      case _1 :: Nil =>
        val umTree = if (isString(weakTypeOf[A])) materializeFromString(B, _1) else materializeStrict(B, _1)
        c.Expr[Unmarshaller[A, B]](umTree)
      case _ => c.abort(c.enclosingPosition, "To materialize unmarshaller, case class must have arity == 1")
    }
  }

  private def materializeFromString(caseClassType: Type, caseAccessor: MethodSymbol) = {
    val um = materializeStrict(caseClassType, caseAccessor)
    if (isString(caseAccessor.returnType)) um else q"${_this}.kebsFromStringUnmarshaller($um)"
  }

  private def materializeStrict(caseClassType: Type, caseAccessor: MethodSymbol) = {
    val from = caseAccessor.typeSignatureIn(caseClassType).resultType
    val to   = caseClassType

    val f = q"${companion(caseClassType)}(_)"

    q"_root_.akka.http.scaladsl.unmarshalling.Unmarshaller.strict[$from, $to]($f)"
  }

  private def materializeAny(caseObjectType: Type) = {
    val from = definitions.AnyTpe
    val f    = q"(_ => ${caseObjectType.termSymbol})"

    q"_root_.akka.http.scaladsl.unmarshalling.Unmarshaller.strict[$from, $caseObjectType]($f)"
  }
}
