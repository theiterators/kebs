package pl.iterators.kebs.circe

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
import io.circe.derivation.Default
import io.circe.HCursor
import io.circe.DecodingFailure

import io.circe._
import io.circe.syntax._
import pl.iterators.kebs.circe.MacroUtils.abortImplicit
import io.circe.Decoder.Result

object MacroUtils {
  transparent inline def abortImplicit: Nothing = ${abortImplicitImpl}

    private def abortImplicitImpl(using Quotes): Expr[Nothing] =
  quotes.reflect.report.errorAndAbort("Flat format preferred")
}
class KebsCirceMacros {

  transparent inline given materializeDecoder[T](using mirror: Mirror.Of[T]): ConfiguredDecoder[T] = {
    lazy val name = constValue[mirror.MirroredLabel]
    lazy val elemLabels: List[String] = summonLabels[mirror.MirroredElemLabels]

  elemLabels match
    case Nil => abortImplicit
    case _1 :: Nil => if(preferFlat) abortImplicit else _materializeDecoder
    case fields => _materializeDecoder
}

 transparent inline def _materializeDecoder[T](using mirror: Mirror.Of[T]): ConfiguredDecoder[T] = {
  new ConfiguredDecoder[T] {

          val name = constValue[mirror.MirroredLabel]
      lazy val elemLabels: List[String] = summonLabels[mirror.MirroredElemLabels].map(namingStrategy.transformMemberNames)
      lazy val elemDecoders: List[Decoder[?]] = summonDecoders[mirror.MirroredElemTypes]
      lazy val elemDefaults: Default[T] = Predef.summon[Default[T]]
    override def apply(c: HCursor): Result[T] = {
      inline mirror match {
        case product: Mirror.ProductOf[T] =>
          for {
            args <- elemDecoders.zip(elemLabels).foldLeft(Right(Nil): Decoder.Result[List[Any]]) { 
              case (acc, (decoder, key)) =>
                println(acc)
                acc.flatMap(params => decoder.tryDecode(c.downField(key)).map(x => params :+ x))
            }
          } yield product.fromProduct(Tuple.fromArray(args.toArray))
        
        case _: Mirror.SumOf[T] => decodeSum(c)
      }
    }
  }
}



  transparent inline def materializeEncoder[T](using mirror: Mirror.Of[T]): ConfiguredEncoder[T] = {
        if(preferFlat == true) {
            MacroUtils.abortImplicit   
          } else {
new ConfiguredEncoder[T](using namingStrategy) {
    val name = constValue[mirror.MirroredLabel]
    override lazy val elemLabels: List[String] = summonLabels[mirror.MirroredElemLabels].map(namingStrategy.transformMemberNames)
    override lazy val elemEncoders: List[Encoder[?]] = summonEncoders[mirror.MirroredElemTypes]

    override def encodeObject(a: T): JsonObject = 
        inline mirror match
          case product: Mirror.ProductOf[T] => 
            encodeProduct(a)
          case sum: Mirror.SumOf[T]   => encodeSum(sum.ordinal(a), a)
  }
  }
  }

private[circe] inline final def summonLabels[T <: Tuple]: List[String] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  => constValue[t].asInstanceOf[String] :: summonLabels[ts]

private[circe] inline final def summonEncoders[T <: Tuple](using Configuration): List[Encoder[_]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  => summonEncoder[t] :: summonEncoders[ts]

private[circe] inline final def summonEncoder[A](using Configuration): Encoder[A] =
  summonFrom {
    case encodeA: Encoder[A] => encodeA
    case _: Mirror.Of[A]     => ConfiguredEncoder.derived[A]
  }

private[circe] inline final def summonDecoders[T <: Tuple](using Configuration): List[Decoder[_]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  => summonDecoder[t] :: summonDecoders[ts]

private[circe] inline final def summonDecoder[A](using Configuration): Decoder[A] =
  summonFrom {
    case decodeA: Decoder[A] => decodeA
    case _: Mirror.Of[A]     => ConfiguredDecoder.derived[A]
  }

    protected val preferFlat: Boolean                                            = true

  protected implicit val namingStrategy: Configuration = Configuration.default
}

object KebsCirceMacros extends KebsCirceMacros {
  object NoflatVariant extends KebsCirceMacros {
    override protected val preferFlat = false
  }

  object CapitalizedCamelCase extends KebsCirceMacros {

    protected override implicit val namingStrategy: Configuration = Configuration.default.withPascalCaseMemberNames
  }

  object SnakifyVariant extends KebsCirceMacros {

     protected override implicit val namingStrategy: Configuration = Configuration.default.withSnakeCaseMemberNames


  }

  }
