package pl.iterators.kebs.circe

import io.circe.generic.AutoDerivation
import io.circe.{ Decoder, Encoder }
import io.circe.`export`.Exported
import scala.deriving.Mirror
import scala.util.Try
import scala.quoted.Quotes

trait KebsCirce extends AutoDerivation {
  implicit inline final def flatDecoder[T, A](using inline rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
  implicit def flatEncoder[T, A](using rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)
}

object KebsCirce {

  // trait NoFlat extends KebsCirce {
  //   implicit inline def genericNoFlatDecoder[T <: Product](using inline T: Mirror.Of[T]): Decoder[T] = KebsCirceMacros.NoflatVariant().materializeDecoder[T]
  //   implicit inline def genericNoFlatEncoder[T <: Product](using inline T: Mirror.Of[T]): Encoder[T] = KebsCirceMacros.NoflatVariant().materializeEncoder[T]
  // }

  // trait Snakified extends KebsCirce {
  //   implicit inline def genericSnakifiedDecoder[T <: Product](using inline T: Mirror.Of[T]): Decoder[T] = KebsCirceMacros.SnakifyVariant.materializeDecoder[T]
  //   implicit inline def genericSnakifiedEncoder[T <: Product](using inline T: Mirror.Of[T]): Encoder[T] = KebsCirceMacros.SnakifyVariant.materializeEncoder[T]
  // }

  // trait Capitalized extends KebsCirce {
  //   implicit inline def genericCapitalizedDecoder[T <: Product](using inline T: Mirror.Of[T]): Decoder[T] =  KebsCirceMacros.CapitalizedCamelCase().materializeDecoder[T]
  //   implicit inline def genericCapitalizedEncoder[T <: Product](using inline T: Mirror.Of[T]): Encoder[T] = KebsCirceMacros.CapitalizedCamelCase().materializeEncoder[T]
  // }
}
