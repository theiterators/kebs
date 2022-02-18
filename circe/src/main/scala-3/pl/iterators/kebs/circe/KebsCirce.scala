package pl.iterators.kebs.circe

import io.circe.{ Decoder, Encoder }
import scala.deriving._
import scala.util.Try
import scala.quoted.Quotes
import io.circe.HCursor
import pl.iterators.kebs.macros.CaseClass1Rep
import io.circe.generic.AutoDerivation

trait KebsCirce extends AutoDerivation {

  implicit inline given[T, A](using rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))

  implicit inline given[T, A](using rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)
}

object KebsCirce {

  trait NoFlat extends KebsCirce {

  }

  trait Snakified extends KebsCirce {
  }

  trait Capitalized extends KebsCirce {
  }
}
