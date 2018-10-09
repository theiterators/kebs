package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.macros.CaseClass1Rep

import scala.language.experimental.macros
import scala.util.Try

trait KebsCirce {
  implicit def flatDecoder[T, A](implicit rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
  implicit def flatEncoder[T, A](implicit rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] = encoder.contramap(rep.unapply)

  implicit def genericDecoder[T <: Product]: Decoder[T] = macro KebsCirceMacros.materializeDecoder[T]
  implicit def genericEncoder[T <: Product]: Encoder[T] = macro KebsCirceMacros.materializeEncoder[T]
}

object KebsCirce {
  trait NoFlat extends KebsCirce {
    implicit def genericNoFlatDecoder[T <: Product]: Decoder[T] = macro KebsCirceMacros.NoflatVariant.materializeDecoder[T]
    implicit def genericNoFlatEncoder[T <: Product]: Encoder[T] = macro KebsCirceMacros.NoflatVariant.materializeEncoder[T]
  }

  trait Snakified extends KebsCirce {
    implicit def genericSnakifiedDecoder[T <: Product]: Decoder[T] = macro KebsCirceMacros.SnakifyVariant.materializeDecoder[T]
    implicit def genericSnakifiedEncoder[T <: Product]: Encoder[T] = macro KebsCirceMacros.SnakifyVariant.materializeEncoder[T]
  }
}
