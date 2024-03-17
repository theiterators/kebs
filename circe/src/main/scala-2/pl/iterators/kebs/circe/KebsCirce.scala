package pl.iterators.kebs.circe

import io.circe.generic.AutoDerivation
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

import scala.language.experimental.macros
import scala.util.Try

trait KebsCirce extends AutoDerivation with CaseClass1ToValueClass {
  implicit def flatDecoder[T, A](implicit rep: ValueClassLike[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))
  implicit def flatEncoder[T, A](implicit rep: ValueClassLike[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

  implicit def instanceConverterEncoder[T, A](implicit rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

  implicit def instanceConverterDecoder[T, A](implicit rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))
}

object KebsCirce {

  trait Snakified extends KebsCirce {
    implicit def genericSnakifiedDecoder[T <: Product]: Decoder[T] = macro KebsCirceMacros.SnakifyVariant.materializeDecoder[T]
    implicit def genericSnakifiedEncoder[T <: Product]: Encoder[T] = macro KebsCirceMacros.SnakifyVariant.materializeEncoder[T]
  }

  trait Capitalized extends KebsCirce {
    implicit def genericCapitalizedDecoder[T <: Product]: Decoder[T] = macro KebsCirceMacros.CapitalizedCamelCase.materializeDecoder[T]
    implicit def genericCapitalizedEncoder[T <: Product]: Encoder[T] = macro KebsCirceMacros.CapitalizedCamelCase.materializeEncoder[T]
  }
}
