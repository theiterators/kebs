package pl.iterators.kebs.circe

import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.naming.Naming

trait KebsCirce extends KebsCirceAutoCompat {
  // read through the protocol, not implicitly: another protocol imported in the same scope would make it ambiguous
  def kebsJsonConfig: JsonConfig = JsonConfig.default

  implicit def flatDecoder[T, A](implicit rep: ValueClassLike[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(rep.from)
  implicit def flatEncoder[T, A](implicit rep: ValueClassLike[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.unapply)

  implicit def instanceConverterEncoder[T, A](implicit rep: InstanceConverter[T, A], encoder: Encoder[A]): Encoder[T] =
    encoder.contramap(rep.encode)
  implicit def instanceConverterDecoder[T, A](implicit rep: InstanceConverter[T, A], decoder: Decoder[A]): Decoder[T] =
    decoder.emap(rep.decodeEither)

  trait KebsCirceSnakified extends KebsCirce {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withFieldNaming(Naming.snakeCase)
  }

  trait KebsCirceCapitalized extends KebsCirce {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withFieldNaming(Naming.pascalCase)
  }
}
