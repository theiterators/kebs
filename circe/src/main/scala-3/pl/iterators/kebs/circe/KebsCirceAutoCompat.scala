package pl.iterators.kebs.circe

import hearth.kindlings.circederivation.{KindlingsDecoder, KindlingsEncoder}
import io.circe.`export`.Exported
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.circe.internal.KebsCirceConfig

import scala.deriving.Mirror

// Exported: circe's low-priority hook, so instances in a type's companion win over derivation
private[circe] trait KebsCirceAutoCompat { this: KebsCirce =>
  inline implicit def kebsAutoEncoder[A](using inline m: Mirror.Of[A]): Exported[Encoder[A]] =
    Exported(KindlingsEncoder.derived[A](using KebsCirceConfig.toKindlings(kebsJsonConfig)))

  // circe prefers the AsObject route for a plain Encoder request too, so it must only match types that encode to an
  // object: products with fields and sealed sums (a case object goes through kebsAutoEncoder)
  inline implicit def kebsAutoAsObjectEncoder[A](using
      m: Mirror.ProductOf[A],
      nonEmpty: m.MirroredElemTypes <:< NonEmptyTuple
  ): Exported[Encoder.AsObject[A]] =
    Exported(KindlingsEncoder.deriveAsObject[A](using KebsCirceConfig.toKindlings(kebsJsonConfig)))

  inline implicit def kebsAutoAsObjectSumEncoder[A](using inline m: Mirror.SumOf[A]): Exported[Encoder.AsObject[A]] =
    Exported(KindlingsEncoder.deriveAsObject[A](using KebsCirceConfig.toKindlings(kebsJsonConfig)))

  inline implicit def kebsAutoDecoder[A](using inline m: Mirror.Of[A]): Exported[Decoder[A]] =
    Exported(KindlingsDecoder.derived[A](using KebsCirceConfig.toKindlings(kebsJsonConfig)))
}
