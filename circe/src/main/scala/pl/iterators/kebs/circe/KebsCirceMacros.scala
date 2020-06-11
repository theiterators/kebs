package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder}
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
      case _ =>
        q"""_root_.io.circe.generic.auto.exportDecoder[$T].instance"""
    }
    c.Expr[Decoder[T]](decoder)
  }

  final def materializeEncoder[T: c.WeakTypeTag]: c.Expr[Encoder[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Encoder, ${T.typeSymbol} must be a case class")
    val encoder = caseAccessors(T) match {
      case Nil =>
        q"""_root_.io.circe.Encoder.instance[${T.typeSymbol}](_ => _root_.io.circe.Json.fromJsonObject(_root_.io.circe.JsonObject.empty))"""
      case _ :: Nil if preferFlat && isLookingFor(encoderOf(T)) && !noflat(T) =>
        c.abort(c.enclosingPosition, "Flat format preferred")
      case _ =>
        q"""_root_.io.circe.generic.auto.exportEncoder[$T].instance"""

    }
    c.Expr[Encoder[T]](encoder)
  }

  private val noflatType = typeOf[noflat]

  private def isLookingFor(t: Type) = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == t.typeSymbol)

  private def noflat(t: Type) = t.typeSymbol.annotations.exists(_.tree.tpe =:= noflatType)

  private val decoderType = typeOf[Decoder[_]]
  private val encoderType = typeOf[Encoder[_]]

  private def decoderOf(p: Type) = appliedType(decoderType, p)

  private def encoderOf(p: Type) = appliedType(encoderType, p)

  private def extractFieldNames(fields: List[MethodSymbol]) = fields.map(_.name.decodedName.toString)

  protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] = extractFieldNames(fields)

  protected val preferFlat: Boolean = true
}

object KebsCirceMacros {

  class NoflatVariant(context: whitebox.Context) extends KebsCirceMacros(context) {
    override protected val preferFlat = false
  }

  class SnakifyVariant(context: whitebox.Context) extends KebsCirceMacros(context) {

    import SnakifyVariant.snakify
    import c.universe._

    override protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] =
      super.extractJsonFieldNames(fields).map(snakify)
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
