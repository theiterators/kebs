package pl.iterators.kebs.circe

import io.circe.{ Decoder, Encoder }
import scala.deriving._
import scala.util.Try
import scala.quoted.Quotes
import io.circe.HCursor
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.instances.InstanceConverter
import io.circe.generic.AutoDerivation

trait KebsCirce extends AutoDerivation {
   inline implicit def flatDecoder[T, A](implicit inline  rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))

   inline implicit def flatEncoder[T, A](implicit inline rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

   inline implicit def instanceEncoder[T, A](implicit inline rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

   inline implicit def instanceDecoder[T, A](implicit inline rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))
}
