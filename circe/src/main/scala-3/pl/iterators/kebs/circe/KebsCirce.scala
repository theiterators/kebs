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
  transparent inline given[T <: Product](using inline m: Mirror.Of[T]): Decoder[T] = KebsCirceMacros.NoflatVariant.materializeDecoder[T]
  transparent inline given[T <: Product](using m: Mirror.Of[T]): Encoder[T] = KebsCirceMacros.NoflatVariant.materializeEncoder[T]
  }

  trait Snakified extends KebsCirce {
    transparent inline given[T <: Product](using m: Mirror.Of[T]): Decoder[T] = KebsCirceMacros.SnakifyVariant.materializeDecoder[T]
    transparent inline given[T <: Product](using m: Mirror.Of[T]): Encoder[T] = KebsCirceMacros.SnakifyVariant.materializeEncoder[T]
  }

  trait Capitalized extends KebsCirce {
    transparent inline given[T <: Product](using inline m: Mirror.Of[T]): Decoder[T] = KebsCirceMacros.CapitalizedCamelCase.materializeDecoder[T]
    transparent inline given[T <: Product](using m: Mirror.Of[T]): Encoder[T] = KebsCirceMacros.CapitalizedCamelCase.materializeEncoder[T]
  }
}
