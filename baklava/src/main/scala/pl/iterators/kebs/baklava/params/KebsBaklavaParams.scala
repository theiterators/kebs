package pl.iterators.kebs.baklava.params

import pl.iterators.baklava.*
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

trait KebsBaklavaParams {
  implicit def toQueryParamValueClassLike[T, U](implicit ev: ValueClassLike[T, U], tsm: ToQueryParam[U]): ToQueryParam[T] =
    new ToQueryParam[T] {
      override def apply(t: T): Seq[String] = tsm(ev.unapply(t))
    }

  implicit def toQueryParamInstanceConverter[T, U](implicit ev: InstanceConverter[T, U], tsm: ToQueryParam[U]): ToQueryParam[T] =
    new ToQueryParam[T] {
      override def apply(t: T): Seq[String] = tsm(ev.encode(t))
    }

  implicit def toPathParamValueClassLike[T, U](implicit ev: ValueClassLike[T, U], tsm: ToPathParam[U]): ToPathParam[T] =
    new ToPathParam[T] {
      override def apply(t: T): String = tsm(ev.unapply(t))
    }

  implicit def toPathParamInstanceConverter[T, U](implicit ev: InstanceConverter[T, U], tsm: ToPathParam[U]): ToPathParam[T] =
    new ToPathParam[T] {
      override def apply(t: T): String = tsm(ev.encode(t))
    }

  implicit def toHeaderValueClassLike[T, U](implicit ev: ValueClassLike[T, U], tsm: ToHeader[U]): ToHeader[T] =
    new ToHeader[T] {
      override def apply(t: T): Option[String] = tsm(ev.unapply(t))

      override def unapply(value: String): Option[T] = tsm.unapply(value).map(ev.apply)
    }

  implicit def toHeaderInstanceConverter[T, U](implicit ev: InstanceConverter[T, U], tsm: ToHeader[U]): ToHeader[T] =
    new ToHeader[T] {
      override def apply(t: T): Option[String] = tsm(ev.encode(t))

      override def unapply(value: String): Option[T] = tsm.unapply(value).map(ev.decode)
    }
}
