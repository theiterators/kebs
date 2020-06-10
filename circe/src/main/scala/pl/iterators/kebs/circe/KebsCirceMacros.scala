package pl.iterators.kebs.circe

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import pl.iterators.kebs.macros.MacroUtils

import scala.collection.immutable.Seq
import scala.reflect.macros.whitebox

class KebsCirceMacros(override val c: whitebox.Context) extends MacroUtils {
  import c.universe._

  final def materializeDecoder[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Decoder, ${T.typeSymbol} must be a case class")

    val decoder = caseAccessors(T) match {
      case Nil =>
        q"""_root_.io.circe.Decoder.decodeJsonObject.emap(obj => if(obj.isEmpty) Right(${T.termSymbol}) else Left("Empty JsonObject"))"""
      case _ :: Nil if preferFlat && isLookingFor(decoderOf(T)) && !noflat(T) =>
        c.abort(c.enclosingPosition, "Flat format preferred")
      case _1 :: Nil =>
        _materializeDecoder(T, List(_1))
      case fields =>
        _materializeDecoder(T, fields)
    }
    c.Expr[Decoder[T]](decoder)
  }

  private def _materializeDecoder(T: Type, fields: List[MethodSymbol]) = {
    val Ps             = extractFieldTypes(fields, T)
    val jsonFieldNames = extractJsonFieldNames(fields)
    if (isRecursiveSearch) c.abort(c.enclosingPosition, "Recursive decoders are not supported")

    if (fields.lengthCompare(maxCaseClassFields) <= 0) {
      val term = TermName(s"forProduct${fields.length}")
      val tree = q"""_root_.io.circe.Decoder.$term[$T, ..$Ps](..$jsonFieldNames)(${apply(T)})"""
      q"$tree(..${inferDecoderFormats(Ps)})"
    } else {
      q"""new _root_.io.circe.Decoder[$T] {
            final def apply(c: _root_.io.circe.HCursor): _root_.io.circe.Decoder.Result[$T] = {
              ???
            }
         }
       """
    }
  }

  final def materializeEncoder[T: c.WeakTypeTag]: c.Expr[Encoder[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Encoder, ${T.typeSymbol} must be a case class")
    val encoder = caseAccessors(T) match {
      case Nil =>
        q"""_root_.io.circe.Encoder.instance[${T.typeSymbol}](_ => _root_.io.circe.Json.fromJsonObject(_root_.io.circe.JsonObject.empty))"""
      case _ :: Nil if preferFlat && isLookingFor(encoderOf(T)) && !noflat(T) => c.abort(c.enclosingPosition, "Flat format preferred")
      case _1 :: Nil                                                          => _materializeEncoder(T, List(_1))
      case fields                                                             => _materializeEncoder(T, fields)
    }
    c.Expr[Encoder[T]](encoder)
  }

  private def _materializeEncoder(T: Type, fields: List[MethodSymbol]) = {
    val Ps             = extractFieldTypes(fields, T)
    val jsonFieldNames = extractJsonFieldNames(fields)

    if (isRecursiveSearch) c.abort(c.enclosingPosition, "Recursive encoders are not supported")

    if (fields.lengthCompare(maxCaseClassFields) <= 0) {
      val term = TermName(s"forProduct${fields.length}")
      val tree = q"""_root_.io.circe.Encoder.$term[$T, ..$Ps](..$jsonFieldNames)(${unapplyFunction(T)})"""
      q"$tree(..${inferEncoderFormats(Ps)})"
    } else {
      val jsonFormats           = inferEncoderFormats(Ps)
      val jsonFieldsWithFormats = jsonFieldNames zip jsonFormats
      val classFieldNames       = extractFieldNames(fields).map(TermName.apply)
      val objVar                = TermName("a")
      val objMap = classFieldNames zip jsonFieldsWithFormats map {
        case (classField, (jsonField, jf)) => q"($jsonField, $jf.apply($objVar.$classField))"
      }
      val x = q"""
         new _root_.io.circe.Encoder[$T] {
          final def apply(a: $T): _root_.io.circe.Json = _root_.io.circe.Json.obj(..$objMap)
         }
       """
      x
    }
  }

  private val noflatType                                                       = typeOf[noflat]
  private def isLookingFor(t: Type)                                            = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == t.typeSymbol)
  private def noflat(t: Type)                                                  = t.typeSymbol.annotations.exists(_.tree.tpe =:= noflatType)
  private val decoderType                                                      = typeOf[Decoder[_]]
  private val encoderType                                                      = typeOf[Encoder[_]]
  private def decoderOf(p: Type)                                               = appliedType(decoderType, p)
  private def encoderOf(p: Type)                                               = appliedType(encoderType, p)
  private def extractFieldTypes(fields: List[MethodSymbol], in: Type)          = fields.map(resultType(_, in))
  private def extractFieldNames(fields: List[MethodSymbol])                    = fields.map(_.name.decodedName.toString)
  protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] = extractFieldNames(fields)
  private def inferDecoderFormats(ps: List[Type])                              = ps.map(p => inferImplicitValue(decoderOf(p), s"Cannot infer Decoder[$p]"))
  private def inferEncoderFormats(ps: List[Type])                              = ps.map(p => inferImplicitValue(encoderOf(p), s"Cannot infer Encoder[$p]"))
  private def isRecursiveSearch = c.enclosingImplicits match {
    case Nil          => false
    case head :: tail => tail.exists(_.pt =:= head.pt)
  }

  protected val preferFlat: Boolean = true
}

object KebsCirceMacros {
  class NoflatVariant(context: whitebox.Context) extends KebsCirceMacros(context) {
    override protected val preferFlat = false
  }
  class SnakifyVariant(context: whitebox.Context) extends KebsCirceMacros(context) {
    import SnakifyVariant.snakify
    import c.universe._

    override protected def extractJsonFieldNames(fields: List[MethodSymbol]) = super.extractJsonFieldNames(fields).map(snakify)
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
