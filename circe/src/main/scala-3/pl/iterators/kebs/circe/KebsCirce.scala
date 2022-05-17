package pl.iterators.kebs.circe

import io.circe.{ Decoder, Encoder }
import scala.deriving._
import scala.util.Try
import scala.quoted.Quotes
import io.circe.HCursor
import pl.iterators.kebs.macros.CaseClass1Rep
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredEncoder
import io.circe.derivation.ConfiguredCodec
import io.circe.derivation.ConfiguredDecoder

trait KebsCirce {

  implicit inline given[T, A](using inline rep: CaseClass1Rep[T, A], inline A: Mirror.Of[A]): Decoder[T] =
    Decoder.derived[A].emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))

  implicit inline given[T, A](using inline A: Mirror.Of[A], rep: CaseClass1Rep[T, A]): Encoder[T] =
    Encoder.AsObject.derived[A].contramap(rep.unapply(_))
    
}

object KebsCirce {

  trait NoFlat extends KebsCirce {

  }

  trait Snakified extends KebsCirce {
  }

  trait Capitalized extends KebsCirce {
  }
}
