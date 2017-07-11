package pl.iterators.kebs.json.macros

import pl.iterators.kebs.json.noflat
import pl.iterators.kebs.macros.MacroUtils
import spray.json.{JsonFormat, RootJsonFormat}

import scala.collection.immutable.Seq
import scala.reflect.macros._

class KebsSprayMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  private val noflatType            = typeOf[noflat]
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

  private def materializeJsonFormat0(T: Type) = q"${_this}.jsonFormat0[$T](() => ${T.termSymbol})"

  private def extractFieldTypes(fields: List[MethodSymbol], in: Type)      = fields.map(_.typeSignatureIn(in).resultType)
  protected def extractFieldNames(fields: List[MethodSymbol]): Seq[String] = fields.map(_.name.decodedName.toString)
  private def materializeRootJsonFormat(T: Type, fields: List[MethodSymbol]) = {
    val Ps             = extractFieldTypes(fields, T)
    val jsonFieldNames = extractFieldNames(fields)

    if (fields.size <= maxCaseClassFields) {
      q"${_this}.jsonFormat[..$Ps, $T](${apply(T)}, ..$jsonFieldNames)"
    } else {
      val classFieldNames = fields.map(_.name.decodedName.toString)
      val reader =
        q"""new _root_.spray.json.RootJsonReader[$T] {
              import _root_.spray.json._
              def read(json: _root_.spray.json.JsValue): $T = json match {
                case _: _root_.spray.json.JsObject =>
                  ${apply(T)}(
                    ..${(classFieldNames zip Ps zip jsonFieldNames).map {
          case ((classField, fieldType), jsonField) =>
            q"${TermName(classField)} = json.getField[$fieldType]($jsonField)"
        }}
                  )
                case _ => deserializationError("JSON object expected")
              }
            }
         """

      val writer =
        q"""new _root_.spray.json.RootJsonWriter[$T] {
              import _root_.spray.json._
              def write(obj: $T): _root_.spray.json.JsValue =
                _root_.spray.json.JsObject(_root_.scala.Predef.Map(
                  ..${(classFieldNames zip jsonFieldNames).map {
          case (classField, jsonField) => q"$jsonField -> obj.${TermName(classField)}.toJson"
        }}
                ))
            }
         """

      q"${_this}.rootJsonFormat[$T]($reader, $writer)"
    }
  }

  final def materializeFlatFormat[T: c.WeakTypeTag]: c.Expr[JsonFormat[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize JsonFormat, ${T.typeSymbol} must be a case class")

    T match {
      case Product1(_1) => materializeJsonFormat[T](T, _1)
      case _            => c.abort(c.enclosingPosition, "To materialize flat JsonFormat, case class must have arity == 1")
    }
  }

  protected val preferFlat: Boolean = true

  final def materializeRootFormat[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize RootJsonFormat, ${T.typeSymbol} must be a case class")

    def isLookingFor(t: Type) = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == t.typeSymbol)
    def noflat(t: Type)       = t.typeSymbol.annotations.exists(_.tree.tpe =:= noflatType)

    val jsonFormat = caseAccessors(T) match {
      case Nil => materializeJsonFormat0(T)
      case (_1 :: Nil) =>
        if (preferFlat && isLookingFor(jsonFormatOf(T)) && !noflat(T)) c.abort(c.enclosingPosition, "Flat format preferred")
        else materializeRootJsonFormat(T, List(_1))
      case fields => materializeRootJsonFormat(T, fields)
    }
    c.Expr[RootJsonFormat[T]](jsonFormat)
  }

  final def materializeLazyFormat[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize recursive RootJsonFormat, ${T.typeSymbol} must be a case class")

    caseAccessors(T) match {
      case Nil => c.abort(c.enclosingPosition, s"${T.typeSymbol} is case object")
      case fields =>
        val format = materializeRootJsonFormat(T, fields)
        c.Expr[RootJsonFormat[T]](q"""{
          implicit lazy val __jf: ${jsonFormatOf(T)} = ${_this}.lazyFormat($format)
          ${_this}.rootFormat(__jf)
        }""")
    }
  }
}

object KebsSprayMacros {
  class NoflatVariant(context: whitebox.Context) extends KebsSprayMacros(context) {
    override protected val preferFlat = false
  }
  class SnakifyVariant(context: whitebox.Context) extends KebsSprayMacros(context) {
    import SnakifyVariant.snakify
    import c.universe._

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
