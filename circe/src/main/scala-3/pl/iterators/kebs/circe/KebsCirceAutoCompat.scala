package pl.iterators.kebs.circe

import hearth.kindlings.circederivation.{KindlingsDecoder, KindlingsEncoder}
import io.circe.`export`.Exported
import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.circe.internal.KebsCirceConfig
import pl.iterators.kebs.core.json.JsonConfig

import scala.deriving.Mirror

// Exported: circe's low-priority hook, so instances in a type's companion win over derivation
private[circe] trait KebsCirceAutoCompat {
  inline implicit def kebsAutoEncoder[A](using config: JsonConfig, inline m: Mirror.Of[A]): Exported[Encoder[A]] =
    Exported(KindlingsEncoder.derived[A](using KebsCirceConfig.toKindlings(config)))

  // circe prefers the AsObject route for a plain Encoder request too, so it must only match types that encode to an
  // object: products with fields and sealed sums (a case object goes through kebsAutoEncoder)
  inline implicit def kebsAutoAsObjectEncoder[A](using
      config: JsonConfig,
      m: Mirror.ProductOf[A],
      nonEmpty: m.MirroredElemTypes <:< NonEmptyTuple
  ): Exported[Encoder.AsObject[A]] =
    Exported(KindlingsEncoder.deriveAsObject[A](using KebsCirceConfig.toKindlings(config)))

  inline implicit def kebsAutoAsObjectSumEncoder[A](using config: JsonConfig, inline m: Mirror.SumOf[A]): Exported[Encoder.AsObject[A]] =
    Exported(KindlingsEncoder.deriveAsObject[A](using KebsCirceConfig.toKindlings(config)))

  inline implicit def kebsAutoDecoder[A](using config: JsonConfig, inline m: Mirror.Of[A]): Exported[Decoder[A]] =
    Exported(KindlingsDecoder.derived[A](using KebsCirceConfig.toKindlings(config)))
}
