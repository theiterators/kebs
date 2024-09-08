package pl.iterators.kebs.pureconfig

import pl.iterators.kebs.core.macros.ValueClassLike
import pureconfig.{ConfigReader, ConfigWriter}

trait KebsPureConfig {
  implicit def valueClassLikeReader[T, P](implicit valueClassLike: ValueClassLike[T, P], reader: ConfigReader[P]): ConfigReader[T] =
    reader.map(valueClassLike.apply)
  implicit def valueClassLikeWriter[T, P](implicit valueClassLike: ValueClassLike[T, P], writer: ConfigWriter[P]): ConfigWriter[T] =
    writer.contramap(valueClassLike.unapply)
}
