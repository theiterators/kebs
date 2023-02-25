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
   inline given[T, A](using rep: CaseClass1Rep[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.apply(obj)).toEither.left.map(_.getMessage))

   inline given[T, A](using rep: CaseClass1Rep[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

   inline given[T, A](using rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)

   inline given[T, A](using rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(obj => Try(rep.decode(obj)).toEither.left.map(_.getMessage))
}

object KebsCirce {
  trait NoFlat extends KebsCirce {
    inline given[T <: Product]: Decoder[T] = ${KebsCirceMacros.NoflatVariant.materializeDecoder[T]}
    inline given[T <: Product]: Encoder[T] = ${KebsCirceMacros.NoflatVariant.materializeEncoder[T]}
  }

  trait Snakified extends KebsCirce {
    inline given[T <: Product]: Decoder[T] = ${KebsCirceMacros.SnakifyVariant.materializeDecoder[T]}
    inline given[T <: Product]: Encoder[T] = ${KebsCirceMacros.SnakifyVariant.materializeEncoder[T]}
  }

  trait Capitalized extends KebsCirce {
    inline given[T <: Product]: Decoder[T] = ${KebsCirceMacros.CapitalizedCamelCase.materializeDecoder[T]}
    inline given[T <: Product]: Encoder[T] = ${KebsCirceMacros.CapitalizedCamelCase.materializeEncoder[T]}
  }
}
