package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.core.macros.MacroUtils
import pl.iterators.kebs.core.macros.namingconventions.SnakifyVariant.snakify

import scala.reflect.macros.whitebox

class KebsCirceMacros(override val c: whitebox.Context) extends MacroUtils {

  import c.universe._

  final def materializeDecoder[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Decoder, ${T.typeSymbol} must be a case class")

    val decoder = caseAccessors(T) match {
      case Nil =>
        q"""_root_.io.circe.Decoder.decodeJsonObject.emap(obj => if(obj.isEmpty) Right(${T.termSymbol}) else Left("Empty JsonObject"))"""
      case _1 :: Nil =>
        if (preferFlat && (isLookingFor(decoderOf(T))))
          c.abort(c.enclosingPosition, "Flat format preferred")
        else
          _materializeDecoder(T, List(_1))
      case fields => _materializeDecoder(T, fields)
    }

    c.Expr[Decoder[T]](decoder)
  }

  private def _materializeDecoder(T: Type, fields: List[MethodSymbol]) = {
    if (fields.lengthCompare(maxCaseClassFields) <= 0) {
      val Ps             = extractFieldTypes(fields, T)
      val jsonFieldNames = extractJsonFieldNames(fields)
      val term           = TermName(s"forProduct${fields.length}")
      val tree           = q"""_root_.io.circe.Decoder.$term[$T, ..$Ps](..$jsonFieldNames)(${apply(T)})"""
      q"$tree(..${inferDecoderFormats(Ps)})"
    } else {
      q"""{
           $semiAutoNamingStrategy
           _root_.io.circe.generic.extras.auto.exportDecoder[$T].instance
           }"""
    }
  }

  final def materializeEncoder[T: c.WeakTypeTag]: c.Expr[Encoder[T]] = {
    val T = weakTypeOf[T]
    assertCaseClass(T, s"To materialize Encoder, ${T.typeSymbol} must be a case class")
    val encoder = caseAccessors(T) match {
      case Nil =>
        q"""_root_.io.circe.Encoder.instance[${T.typeSymbol}](_ => _root_.io.circe.Json.fromJsonObject(_root_.io.circe.JsonObject.empty))"""
      case _1 :: Nil =>
        if (preferFlat && (isLookingFor(encoderOf(T))))
          c.abort(c.enclosingPosition, "Flat format preferred")
        else
          _materializeEncoder(T, List(_1))
      case fields => _materializeEncoder(T, fields)
    }
    c.Expr[Encoder[T]](encoder)
  }

  private def _materializeEncoder(T: Type, fields: List[MethodSymbol]): c.universe.Tree = {
    if (fields.lengthCompare(maxCaseClassFields) <= 0) {
      val Ps             = extractFieldTypes(fields, T)
      val jsonFieldNames = extractJsonFieldNames(fields)
      val term           = TermName(s"forProduct${fields.length}")
      val tree           = q"""_root_.io.circe.Encoder.$term[$T, ..$Ps](..$jsonFieldNames)(${unapplyFunction(T)})"""
      q"$tree(..${inferEncoderFormats(Ps)})"
    } else {
      q"""{
           $semiAutoNamingStrategy
           _root_.io.circe.generic.extras.auto.exportEncoder[$T].instance
           }"""
    }
  }

  private def isLookingFor(t: Type) = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == t.typeSymbol)
  private val decoderType           = typeOf[Decoder[_]]
  private val encoderType           = typeOf[Encoder[_]]
  private def decoderOf(p: Type)    = appliedType(decoderType, p)
  private def encoderOf(p: Type)    = appliedType(encoderType, p)
  private def extractFieldTypes(fields: List[MethodSymbol], in: Type) = fields.map(resultType(_, in))
  private def extractFieldNames(fields: List[MethodSymbol])           = fields.map(_.name.decodedName.toString)
  private def inferDecoderFormats(ps: List[Type]) = ps.map(p => inferImplicitValue(decoderOf(p), s"Cannot infer Decoder[$p]"))
  private def inferEncoderFormats(ps: List[Type]) = ps.map(p => inferImplicitValue(encoderOf(p), s"Cannot infer Encoder[$p]"))
  protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] = extractFieldNames(fields)
  protected val preferFlat: Boolean                                            = true
  protected val semiAutoNamingStrategy: Tree                                   =
    q"implicit lazy val __config: _root_.io.circe.generic.extras.Configuration = _root_.io.circe.generic.extras.Configuration.default"
}

object KebsCirceMacros {

  class CapitalizedCamelCase(context: whitebox.Context) extends KebsCirceMacros(context) {
    import c.universe._

    protected override val semiAutoNamingStrategy: Tree =
      q"implicit lazy val __config: _root_.io.circe.generic.extras.Configuration = _root_.io.circe.generic.extras.Configuration.default.copy(_.capitalize)"

    override protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] =
      super.extractJsonFieldNames(fields).map(_.capitalize)
  }

  class SnakifyVariant(context: whitebox.Context) extends KebsCirceMacros(context) {

    import c.universe._
    protected override val semiAutoNamingStrategy: Tree =
      q"implicit lazy val __config: _root_.io.circe.generic.extras.Configuration = _root_.io.circe.generic.extras.Configuration.default.withSnakeCaseMemberNames"

    override protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] =
      super.extractJsonFieldNames(fields).map(snakify)
  }

}
