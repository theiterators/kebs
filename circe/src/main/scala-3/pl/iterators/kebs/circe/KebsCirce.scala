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

  inline given [T, A](using rep: ValueClassLike[T, A], decoder: Decoder[A]): Decoder[T] = {
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
  }

  inline given [T, A](using rep: ValueClassLike[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

  inline given [T, A](using rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

  inline given [T, A](using rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))
}

object KebsCirce {

  trait Snakified extends KebsCirce {
    override implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  }

  trait Capitalized extends KebsCirce {
    override implicit val configuration: Configuration = Configuration.default.withPascalCaseMemberNames
  }
}
