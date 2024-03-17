package pl.iterators.kebs.json.macros

import pl.iterators.kebs.core.macros.MacroUtils
import spray.json.{JsonFormat, JsonReader, JsonWriter, NullOptions, RootJsonFormat}

import scala.collection.immutable.Seq
import scala.reflect.macros._

class KebsSprayMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  private val jsonFormat            = typeOf[JsonFormat[_]]
  private val jsonReader            = typeOf[JsonReader[_]]
  private val jsonWriter            = typeOf[JsonWriter[_]]
  private val nullOptions           = typeOf[NullOptions]
  private def jsonFormatOf(p: Type) = appliedType(jsonFormat, p)
  private def jsonReaderOf(p: Type) = appliedType(jsonReader, p)
  private def jsonWriterOf(p: Type) = appliedType(jsonWriter, p)

  private def materializeJsonFormat0(T: Type) = q"${_this}.jsonFormat0[$T](() => ${T.termSymbol})"

  private def extractFieldTypes(fields: List[MethodSymbol], in: Type)           = fields.map(resultType(_, in))
  private def extractFieldNames(fields: List[MethodSymbol])                     = fields.map(_.name.decodedName.toString)
  protected def extractJsonFieldNames(fields: List[MethodSymbol]): List[String] = extractFieldNames(fields)

  private def inferFormats(ps: List[Type]) = ps.map(p => inferImplicitValue(jsonFormatOf(p), s"Cannot infer JsonFormat[$p]"))
  private def isRecursiveSearch = c.enclosingImplicits match {
    case Nil          => false
    case head :: tail => tail.exists(_.pt =:= head.pt)
  }
  private def materializeRootJsonFormat(T: Type, fields: List[MethodSymbol]) = {
    val Ps             = extractFieldTypes(fields, T)
    val jsonFieldNames = extractJsonFieldNames(fields)

    if (fields.lengthCompare(maxCaseClassFields) <= 0) {
      val tree = q"${_this}.jsonFormat[..$Ps, $T](${apply(T)}, ..$jsonFieldNames)"
      if (isRecursiveSearch) tree else q"$tree(..${inferFormats(Ps)})"
    } else {
      val jsonFormats           = inferFormats(Ps)
      val jsonFieldsWithFormats = jsonFieldNames zip jsonFormats
      val jsonVar               = TermName("json")
      val applyArgs = jsonFieldsWithFormats.map {
        case (jsonField, jf) => q"${_this}._kebs_getField($jsonVar, $jsonField)($jf)"
      }

      val reader = q"($jsonVar: _root_.spray.json.JsValue) => ${apply(T)}(..$applyArgs)"

      val classFieldNames = extractFieldNames(fields).map(TermName.apply)
      val objVar          = TermName("obj")
      val jsFieldList = classFieldNames zip jsonFieldsWithFormats map {
        case (classField, (jsonField, jf)) => q"($jsonField, $jf.write($objVar.$classField))"
      }
      val objMap =
        q"""_root_.scala.Predef.Map(..$jsFieldList).filter {
            case (_, _root_.spray.json.JsNull) => ${_this.tpe <:< nullOptions}
            case _ => true
          }"""

      val writer = q"($objVar: $T) => _root_.spray.json.JsObject($objMap)"

      val jsonFormat = q"${_this}.jsonFormat[$T]($reader, $writer)"
      q"${_this}.rootFormat[$T]($jsonFormat)"
    }
  }

  protected val preferFlat: Boolean = true

  final def materializeRootFormat[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize RootJsonFormat, ${T.typeSymbol} must be a case class")

    def isLookingFor(t: Type) = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == t.typeSymbol)

    val jsonFormat = caseAccessors(T) match {
      case Nil => materializeJsonFormat0(T)
      case (_1 :: Nil) =>
        if (preferFlat && (isLookingFor(jsonFormatOf(T)) || isLookingFor(jsonWriterOf(T)) || isLookingFor(jsonReaderOf(T))))
          c.abort(c.enclosingPosition, "Flat format preferred")
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

  class SnakifyVariant(context: whitebox.Context) extends KebsSprayMacros(context) {
    import pl.iterators.kebs.core.macros.namingconventions.SnakifyVariant.snakify
    import c.universe._

    override protected def extractJsonFieldNames(fields: List[MethodSymbol]) = super.extractJsonFieldNames(fields).map(snakify)
  }

  class CapitalizedCamelCase(context: whitebox.Context) extends KebsSprayMacros(context) {
    import c.universe._

    override protected def extractJsonFieldNames(fields: List[MethodSymbol]) = super.extractJsonFieldNames(fields).map(_.capitalize)
  }
  
}
