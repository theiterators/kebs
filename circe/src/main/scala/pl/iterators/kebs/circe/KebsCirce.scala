package pl.iterators.kebs.circe

import io.circe.generic.AutoDerivation
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.macros.CaseClass1Rep
import scala.language.experimental.macros

import scala.util.Try

trait KebsCirce extends AutoDerivation {
  implicit def flatDecoder[T, A](implicit rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
  implicit def flatEncoder[T, A](implicit rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] = encoder.contramap(rep.unapply)
}

object KebsCirce {
  trait NoFlat extends KebsCirce {
    implicit def noFlatDecoder[T, A](implicit rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] = ???
    implicit def noFlatEncoder[T, A](implicit rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] = ???
  }
}
