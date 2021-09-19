package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder}

import scala.collection.immutable.Seq
import scala.deriving._
import scala.compiletime._
import scala.quoted.*
import language.deprecated.symbolLiterals
 abstract class KebsCirceMacros {



  def caseAccessors[T: Type](using Quotes): List[quotes.reflect.Symbol] = 
    import quotes.reflect.TypeRepr
    TypeRepr.of[T].typeSymbol.caseFields


  protected def assertCaseClass[T: Type](msg: => String)(using Quotes) = {
    import quotes.reflect.*
    val sym         = TypeRepr.of[T].typeSymbol
    val isCaseClass = sym.flags.is(Flags.Case)
    if (!isCaseClass) report.errorAndAbort(msg, Position.ofMacroExpansion)
  }

  inline final def materializeDecoder[T](using quotes: Quotes, m: Mirror.Of[T]): Expr[Decoder[T]] = {
    import quotes.reflect.*

    val T = TypeRepr.of[T]
    assertCaseClass[T](s"To materialize Decoder, ${T.typeSymbol} must be a case class")
    val ev: Expr[Mirror.Of[T]] = Expr.summon[Mirror.Of[T]].get
    
    val decoder = caseAccessors[T] match {
      case Nil =>
       '${_root_.io.circe.Decoder.decodeJsonObject.emap(obj => if(obj.isEmpty) Right(${Expr(t.asInstanceOf[String])}.asInstanceOf[TypeSymbol]}) else Left("Empty JsonObject"))}
      // case _1 :: Nil =>
      //   if (preferFlat && (isLookingFor(decoderOf[T]) && !noflat[T]))
      //    scala.sys.error("Flat format preferred")
      //   else
      //     _materializeDecoder[T](List(_1))
      // case fields => _materializeDecoder[T](fields)
    }

    decoder
  }

  // private def _materializeDecoder[T](fields: List[quotes.reflect.Symbol]) = {
  //   if (fields.lengthCompare(maxCaseClassFields) <= 0) {
  //     val Ps             = extractFieldTypes(fields, T)
  //     val jsonFieldNames = extractJsonFieldNames(fields)
  //     val term           = TermName(s"forProduct${fields.length}")
  //     val tree           = q"""_root_.io.circe.Decoder.$term[$T, ..$Ps](..$jsonFieldNames)(${apply(T)})"""
  //     q"$tree(..${inferDecoderFormats(Ps)})"
  //   } else {
  //     q"""{
  //          $semiAutoNamingStrategy
  //          _root_.io.circe.generic.extras.auto.exportDecoder[$T].instance
  //          }"""
  //   }
  // }

  // final def materializeEncoder[T: c.WeakTypeTag]: Expr[Encoder[T]] = {
  //   val T = weakTypeOf[T]
  //   assertCaseClass(T, s"To materialize Encoder, ${T.typeSymbol} must be a case class")
  //   val encoder = caseAccessors(T) match {
  //     case Nil =>
  //       q"""_root_.io.circe.Encoder.instance[${T.typeSymbol}](_ => _root_.io.circe.Json.fromJsonObject(_root_.io.circe.JsonObject.empty))"""
  //     case _1 :: Nil =>
  //       if (preferFlat && (isLookingFor(encoderOf(T)) && !noflat(T)))
  //         c.abort(c.enclosingPosition, "Flat format preferred")
  //       else
  //         _materializeEncoder(T, List(_1))
  //     case fields => _materializeEncoder(T, fields)
  //   }
  //   c.Expr[Encoder[T]](encoder)
  // }

  // private def _materializeEncoder(T: Type, fields: List[MethodSymbol]): c.universe.Tree = {
  //   if (fields.lengthCompare(maxCaseClassFields) <= 0) {
  //     val Ps             = extractFieldTypes(fields, T)
  //     val jsonFieldNames = extractJsonFieldNames(fields)
  //     val term           = TermName(s"forProduct${fields.length}")
  //     val tree           = q"""_root_.io.circe.Encoder.$term[$T, ..$Ps](..$jsonFieldNames)(${unapplyFunction(T)})"""
  //     q"$tree(..${inferEncoderFormats(Ps)})"
  //   } else {
  //     q"""{
  //          $semiAutoNamingStrategy
  //          _root_.io.circe.generic.extras.auto.exportEncoder[$T].instance
  //          }"""
  //   }
  // }

  // private val noflatType                                                       = typeOf[noflat]
  // private def isLookingFor[T]                                            = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == t.typeSymbol)
  // private def noflat[T]                                                  = t.typeSymbol.annotations.exists(_.tree.tpe =:= noflatType)
  // private val decoderType                                                      = typeOf[Decoder[_]]
  // private val encoderType                                                      = typeOf[Encoder[_]]
  // private def decoderOf[T]                                               = appliedType(decoderType, p)
  // private def encoderOf[T]                                               = appliedType(encoderType, p)
  // private def extractFieldTypes(fields: List[MethodSymbol], in: Type)          = fields.map(resultType(_, in))
  // private def extractFieldNames(fields: List[MethodSymbol])                    = fields.map(_.name.decodedName.toString)
  // private def inferDecoderFormats(ps: List[Type])                              = ps.map(p => inferImplicitValue(decoderOf(p), s"Cannot infer Decoder[$p]"))
  // private def inferEncoderFormats(ps: List[Type])                              = ps.map(p => inferImplicitValue(encoderOf(p), s"Cannot infer Encoder[$p]"))
  // protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] = extractFieldNames(fields)
  // protected val preferFlat: Boolean                                            = true
  // protected val semiAutoNamingStrategy: Tree =
  //   q"implicit lazy val __config: _root_.io.circe.generic.extras.Configuration = _root_.io.circe.generic.extras.Configuration.default"

}

// object KebsCirceMacros {
//   class NoflatVariant extends KebsCirceMacros {
//     override protected val preferFlat = false
//   }
  
//   class CapitalizedCamelCase extends KebsCirceMacros {


//     protected override val semiAutoNamingStrategy: Tree =
//       q"implicit lazy val __config: _root_.io.circe.generic.extras.Configuration = _root_.io.circe.generic.extras.Configuration.default.copy(_.capitalize)"

//     override protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] =
//       super.extractJsonFieldNames(fields).map(_.capitalize)
//   }

//   class SnakifyVariant extends KebsCirceMacros {

//     import pl.iterators.kebs.macros.namingconventions.SnakifyVariant.snakify
    
//     protected override val semiAutoNamingStrategy: Tree =
//       q"implicit lazy val __config: _root_.io.circe.generic.extras.Configuration = _root_.io.circe.generic.extras.Configuration.default.withSnakeCaseMemberNames"

//     override protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] =
//       super.extractJsonFieldNames(fields).map(snakify)
//   }

// }
