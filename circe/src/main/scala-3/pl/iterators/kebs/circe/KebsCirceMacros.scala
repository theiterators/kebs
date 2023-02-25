package pl.iterators.kebs.circe

// import io.circe.Configuration
import io.circe.{Decoder, Encoder}

import scala.collection.immutable.Seq
import scala.reflect.macros.whitebox
import scala.quoted.*
import scala.deriving.Mirror
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredDecoder
import io.circe.derivation.ConfiguredEncoder
import scala.compiletime.{ codeOf, constValue, erasedValue, error, summonFrom, summonInline }
import scala.deriving.Mirror


class KebsCirceMacros {

  def materializeDecoder[T](using quotes: Quotes, tpe: Type[T]): Expr[Decoder[T]] = {
   import quotes.reflect.*
   val tpe = TypeRepr.of[T]
   val tpeSym = TypeTree.of[T].symbol
   if !tpeSym.flags.is(Flags.Case) then report.errorAndAbort(s"To materialize Decoder, ${tpeSym.fullName} must be a case class") else ()

    val fields: List[Symbol]             = TypeRepr.of[T].typeSymbol.caseFields
    val fieldTypeTrees: List[TypeTree]   = fields.map(_.tree.asInstanceOf[ValDef].tpt)
    val decoderTerms: List[Term]         = fieldTypeTrees.map(lookupDecoderFor(_))
    val decoders: Expr[List[Decoder[_]]] = Expr.ofList(decoderTerms.map(_.asExprOf[Decoder[_]]))
    val typeSymbol: Symbol = TypeRepr.of[T].typeSymbol

   val decoder = fields match
    case Nil => '{Decoder.decodeJsonObject.emap(obj => if(obj.isEmpty) Right(${New(TypeTree.of[T]).asExprOf[T]}) else Left("Empty JsonObject"))}
    case _1 :: Nil => 
      if(preferFlat && isLookingFor(typeSymbol.tree) && !noflat(typeSymbol))
        report.errorAndAbort("Flat format preferred", Position.ofMacroExpansion)
      else
       _materializeDecoder(typeSymbol, List(_1))
    case some => _materializeDecoder(typeSymbol, fields)
   
    decoder.asExprOf[Decoder[T]]
  }

  private def _materializeDecoder[T](using Quotes)(using tp: Type[T])(tpe: quotes.reflect.Symbol, fields: List[quotes.reflect.Symbol]): Expr[ConfiguredDecoder[T]] = {
    // import quotes.reflect.*
    // if (fields.lengthCompare(maxCaseClassFields) <= 0) {
    //   // val ps = fields.map(f => TypeTree.ref(TypeRepr.of[T].memberType(f).typeSymbol))
    //   val ps = fields.map(f => TypeRepr.of[T].memberType(f).typeSymbol)
    //   val jsonFieldNames = Expr(fields.map(_.name))
    //   // val tree           = '{_root_.io.circe.Decoder.forProduct${fields.length}[T, ..ps]($jsonFieldNames)(_ => ${New(TypeTree.of[T]).asExprOf[T]})}
    // //  val tree = ??? //TODO write AST for Decoder.forProduct
    //   // '{$tree(${inferDecoderFormats(Ps)})}
    // } else {
      '{_root_.io.circe.derivation.ConfiguredDecoder.derived[T](using $semiAutoNamingStrategy)(using ${Expr.summon[Mirror.Of[T]].get})}
    }

  final def materializeEncoder[T](using quotes: Quotes, tpe: Type[T]): Expr[Encoder[T]] = {
   import quotes.reflect.*
   val tpe = TypeRepr.of[T]
   val tpeSym = TypeTree.of[T].symbol
   if !tpeSym.flags.is(Flags.Case) then report.errorAndAbort(s"To materialize Decoder, ${tpeSym.fullName} must be a case class") else ()

       val fields: List[Symbol]             = TypeRepr.of[T].typeSymbol.caseFields
    val fieldTypeTrees: List[TypeTree]   = fields.map(_.tree.asInstanceOf[ValDef].tpt)
    val encoderTerms: List[Term]         = fieldTypeTrees.map(lookupEncoderFor(_))
    val decoders: Expr[List[Encoder[_]]] = Expr.ofList(encoderTerms.map(_.asExprOf[Encoder[_]]))
    val typeSymbol: Symbol = TypeRepr.of[T].typeSymbol

   val encoder = fields match
    case Nil =>
       '{Encoder.instance[T](_ => _root_.io.circe.Json.fromJsonObject(_root_.io.circe.JsonObject.empty))}
    case _1 :: Nil => 
      if(preferFlat && isLookingFor(typeSymbol.tree) && !noflat(typeSymbol))
        report.errorAndAbort("Flat format preferred", Position.ofMacroExpansion)
      else
       _materializeEncoder(typeSymbol, List(_1))
    case some => _materializeEncoder(typeSymbol, fields)
   
    encoder.asExprOf[Encoder[T]]
  }

  private def _materializeEncoder[T](using Quotes)(using tp: Type[T])(tpe: quotes.reflect.Symbol, fields: List[quotes.reflect.Symbol]): Expr[ConfiguredEncoder[T]] = {
    // if (fields.lengthCompare(maxCaseClassFields) <= 0) {
    //   val Ps             = extractFieldTypes(fields, T)
    //   val jsonFieldNames = extractJsonFieldNames(fields)
    //   val term           = TermName(s"forProduct${fields.length}")
    //   val tree           = q"""_root_.io.circe.Encoder.$term[$T, ..$Ps](..$jsonFieldNames)(${unapplyFunction(T)})"""
    //   q"$tree(..${inferEncoderFormats(Ps)})"
    // } else {
      '{
           implicit val config: Configuration = $semiAutoNamingStrategy

           _root_.io.circe.derivation.ConfiguredEncoder.derived[T](using config)(using ${Expr.summon[Mirror.Of[T]].get})
           }
  }

    private def isLookingFor(using Quotes)(t: quotes.reflect.Tree): Boolean = {
    import quotes.reflect.*
    val tpe: TypeTree = Applied(TypeTree.of[Decoder], List(t))
    Implicits.search(tpe.tpe) match {
      case res: ImplicitSearchSuccess => true
    }
  }

  private def lookupDecoderFor(using Quotes)(t: quotes.reflect.Tree): quotes.reflect.Term = {
    import quotes.reflect.*
    val tpe: TypeTree = Applied(TypeTree.of[Decoder], List(t))
    Implicits.search(tpe.tpe) match {
      case res: ImplicitSearchSuccess => res.tree
    }
  }

  private def lookupEncoderFor(using Quotes)(t: quotes.reflect.Tree): quotes.reflect.Term = {
    import quotes.reflect.*
    val tpe: TypeTree = Applied(TypeTree.of[Encoder], List(t))
    Implicits.search(tpe.tpe) match {
      case res: ImplicitSearchSuccess => res.tree
    }
  }

  private def noflatType(using Quotes)                                                       = {
       import quotes.reflect.*
   TypeRepr.of[noflat]
  }
  private def noflat(using Quotes)(t: quotes.reflect.Symbol)                                                  = t.annotations.exists(_.tpe =:= noflatType)
    protected val maxCaseClassFields = 22

  private def extractFieldNames[T: Type](using q: Quotes) = {
  import q.reflect.*
  val fields = TypeTree.of[T].symbol.caseFields.map(_.name)

  Expr(fields)
  }
  // private def inferDecoderFormats(ps: List[Type])                              = ps.map(p => inferImplicitValue(decoderOf(p), s"Cannot infer Decoder[$p]"))
  // private def inferEncoderFormats(ps: List[Type])                              = ps.map(p => inferImplicitValue(encoderOf(p), s"Cannot infer Encoder[$p]"))
  // protected def extractJsonFieldNames(fields: List[MethodSymbol]): Seq[String] = extractFieldNames(fields)
  
  protected val preferFlat: Boolean                                            = true

  protected def semiAutoNamingStrategy(using q: Quotes): Expr[Configuration] = {
    import q.reflect.*
    '{_root_.io.circe.derivation.Configuration.default}
}
}

object KebsCirceMacros {

  object NoflatVariant extends KebsCirceMacros {
    override protected val preferFlat = false
  }

  object CapitalizedCamelCase extends KebsCirceMacros {

    protected override def semiAutoNamingStrategy(using Quotes): Expr[Configuration] = '{Configuration.default.copy(_.capitalize)}
  }

  object SnakifyVariant extends KebsCirceMacros {

    protected override def semiAutoNamingStrategy(using Quotes): Expr[Configuration] = '{Configuration.default.withSnakeCaseMemberNames}

  }
  }
