package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder}
import scala.deriving._
import scala.util.Try
import io.circe.derivation.ConfiguredDecoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredEncoder

import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.instances.InstanceConverter

private[circe] trait KebsAutoDerivation {

  implicit val configuration: Configuration = Configuration.default

  inline implicit def exportDecoder[A](using conf: Configuration, inline m: Mirror.ProductOf[A]): ConfiguredDecoder[A] =
    ConfiguredDecoder.derived[A]

  inline implicit def exportEncoder[A](using conf: Configuration, inline m: Mirror.ProductOf[A]): ConfiguredEncoder[A] =
    ConfiguredEncoder.derived[A]
}
trait KebsCirce extends KebsAutoDerivation {

  inline implicit def flatDecoder[T, A](using rep: ValueClassLike[T, A], decoder: Decoder[A]): Decoder[T] = {
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
  }

  inline implicit def flatEncoder[T, A](using rep: ValueClassLike[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

  inline implicit def instanceConverterEncoder[T, A](using rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

  inline implicit def instanceConverterDecoder[T, A](using rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))

  trait KebsCirceSnakified extends KebsCirce {
    override implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  }

  trait KebsCirceCapitalized extends KebsCirce {
    override implicit val configuration: Configuration = Configuration.default.withPascalCaseMemberNames
  }
}
