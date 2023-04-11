package pl.iterators.kebs.circe

import io.circe.{ Decoder, Encoder }
import scala.deriving._
import scala.util.Try
import scala.quoted.Quotes
import io.circe.HCursor
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.instances.InstanceConverter
import io.circe.generic.AutoDerivation
import scala.quoted.Type
import io.circe.derivation.ConfiguredDecoder
import io.circe.derivation.Configuration
import io.circe.Derivation
import io.circe.DecoderDerivation
import io.circe.EncoderDerivation
import io.circe.derivation.ConfiguredEncoder
import scala.NonEmptyTuple

private[circe] trait KebsAutoDerivation {
  
  implicit val configuration: Configuration = Configuration.default

  inline implicit def exportDecoder[A](using conf: Configuration, inline m: Mirror.ProductOf[A]): ConfiguredDecoder[A] =
    ConfiguredDecoder.derived[A]

  inline implicit def exportEncoder[A](using conf: Configuration, inline m: Mirror.ProductOf[A]): ConfiguredEncoder[A] =
    ConfiguredEncoder.derived[A]
}
trait KebsCirce extends KebsAutoDerivation {

   inline given[T, A](using rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] = {
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
   }

   inline given[T, A](using rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

   inline given[T, A](using rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

   inline given[T, A](using rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))
}

object KebsCirce {
  trait NoFlat extends KebsCirce {
  }

  trait Snakified extends KebsCirce {
    override implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  }

  trait Capitalized extends KebsCirce {
    override implicit val configuration: Configuration = Configuration.default.withPascalCaseMemberNames
  }
}
