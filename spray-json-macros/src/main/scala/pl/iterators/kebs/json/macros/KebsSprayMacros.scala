package pl.iterators.kebs.json.macros

import pl.iterators.kebs.macros.MacroUtils
import spray.json.{JsonFormat, RootJsonFormat}

import scala.collection.immutable.Seq
import scala.reflect.macros._

class KebsSprayMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  private val jsonFormat            = typeOf[JsonFormat[_]]
  private def jsonFormatOf(p: Type) = appliedType(jsonFormat, p)

  private def materializeJsonFormat[T](T: Type, field: MethodSymbol) = {
    val P           = field.typeSignatureIn(T).resultType
    val jsonFormatP = jsonFormatOf(P)

    def inferJsonFormat =
      inferImplicitValue(jsonFormatP, s"To materialize JsonFormat for ${T.typeSymbol}, JsonFormat[$P] is needed")

    val readerF = q"(json: _root_.spray.json.JsValue) => ${apply(T)}($inferJsonFormat.read(json))"
    val writerF = q"(obj: $T) => $inferJsonFormat.write(obj.$field)"

    c.Expr[JsonFormat[T]](q"${_this}.constructJsonFormat[$T]($readerF, $writerF)")
  }

  private def materializeJsonFormat0[T](T: Type) = c.Expr[RootJsonFormat[T]](q"${_this}.jsonFormat0[$T](() => ${T.termSymbol})")

  protected def extractFieldNames(fields: List[MethodSymbol]): Seq[String] = fields.map(_.name.decodedName.toString)
  private def materializeRootJsonFormat[T](T: Type, fields: List[MethodSymbol]) = {
    val Ps         = fields.map(_.typeSignatureIn(T).resultType)
    val fieldNames = extractFieldNames(fields)

    c.Expr[RootJsonFormat[T]](q"${_this}.jsonFormat[..$Ps, $T](${apply(T)}, ..$fieldNames)")
  }

  final def materializeFlatFormat[T: c.WeakTypeTag]: c.Expr[JsonFormat[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize JsonFormat, ${T.typeSymbol} must be a case class")

    T match {
      case Product1(_1) => materializeJsonFormat[T](T, _1)
      case _            => c.abort(c.enclosingPosition, "To materialize flat JsonFormat, case class must have arity == 1")
    }
  }

  final def materializeRootFormat[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize RootJsonFormat, ${T.typeSymbol} must be a case class")

    def isLookingFor(t: Type) = c.enclosingImplicits.head.pt.typeSymbol == t.typeSymbol

    caseAccessors(T) match {
      case Nil => materializeJsonFormat0[T](T)
      case (_1 :: Nil) =>
        if (isLookingFor(jsonFormatOf(T))) c.abort(c.enclosingPosition, "Flat format preferred")
        else materializeRootJsonFormat[T](T, List(_1))
      case fields => materializeRootJsonFormat[T](T, fields)
    }
  }

}

object KebsSprayMacros {
  class SnakifyVariant(context: whitebox.Context) extends KebsSprayMacros(context) {
    import c.universe._
    import SnakifyVariant.snakify

    override protected def extractFieldNames(fields: List[MethodSymbol]) = super.extractFieldNames(fields).map(snakify)
  }
  object SnakifyVariant {
    private val PASS_1 = """([A-Z\d]+)([A-Z][a-z])""".r
    private val PASS_2 = """([a-z\d])([A-Z])""".r

    private def isCamelCased(word: String) = word.exists(ch => ch == '-' || ch.isUpper)

    def snakify(word: String): String = {
      if (!isCamelCased(word)) word
      else {
        val afterPass1 = PASS_1.replaceAllIn(word, "$1_$2")
        val afterPass2 = PASS_2.replaceAllIn(afterPass1, "$1_$2")

        afterPass2.replace('-', '_').toLowerCase
      }
    }
  }
}
