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

  implicit inline given[T, A](using inline rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))

  implicit inline given[T, A](using inline rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)
    
  implicit inline given[T, A](using inline rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

  implicit inline given[T, A](using inline rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))
    
}
