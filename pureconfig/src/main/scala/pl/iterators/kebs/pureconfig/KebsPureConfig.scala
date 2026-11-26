package pl.iterators.kebs.pureconfig

import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import pureconfig.error.{CannotConvert, UserValidationFailed}
import pureconfig.{ConfigReader, ConfigWriter}

trait KebsPureConfig {
  implicit def valueClassLikeReader[T, P](implicit valueClassLike: ValueClassLike[T, P], reader: ConfigReader[P]): ConfigReader[T] =
    reader.emap(valueClassLike.from(_).left.map(UserValidationFailed(_)))
  implicit def valueClassLikeWriter[T, P](implicit valueClassLike: ValueClassLike[T, P], writer: ConfigWriter[P]): ConfigWriter[T] =
    writer.contramap(valueClassLike.unapply)

  implicit def instanceConverterReader[T, V](implicit converter: InstanceConverter[T, V], reader: ConfigReader[V]): ConfigReader[T] =
    reader.emap(converter.decodeEither(_).left.map(UserValidationFailed(_)))
  implicit def instanceConverterWriter[T, V](implicit converter: InstanceConverter[T, V], writer: ConfigWriter[V]): ConfigWriter[T] =
    writer.contramap(converter.encode)

  implicit def enumReader[E](implicit e: EnumLike[E], naming: EnumNaming): ConfigReader[E] = {
    val named = naming.applyTo(e)
    ConfigReader[String].emap(name => naming.lookup(named, name).toRight(notOneOf(name, named.names)))
  }
  implicit def enumWriter[E](implicit e: EnumLike[E], naming: EnumNaming): ConfigWriter[E] = {
    val named = naming.applyTo(e)
    ConfigWriter[String].contramap(named.getName)
  }

  implicit def valueEnumReader[V, E <: ValueEnumLikeEntry[V]](implicit e: ValueEnumLike[V, E], reader: ConfigReader[V]): ConfigReader[E] =
    reader.emap(value => e.withValueOption(value).toRight(notOneOf(value, e.values.map(_.value))))
  implicit def valueEnumWriter[V, E <: ValueEnumLikeEntry[V]](implicit e: ValueEnumLike[V, E], writer: ConfigWriter[V]): ConfigWriter[E] =
    writer.contramap(_.value)

  private def notOneOf(value: Any, expected: Seq[Any]): CannotConvert =
    CannotConvert(value.toString, "a value of the enum", s"expected one of: ${expected.mkString(", ")}")
}
