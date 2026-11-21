package pl.iterators.kebs.baklava.params.enums

import pl.iterators.baklava.{ToHeader, ToPathParam, ToQueryParam}
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

import scala.annotation.unused

trait KebsBaklavaEnumsParams {
  implicit def toQueryParamEnum[T](implicit _enum: EnumLike[T]): ToQueryParam[T] = new ToQueryParam[T] {
    override def apply(t: T): Seq[String] = Seq(_enum.getName(t))
  }

  implicit def toPathParamEnum[T](implicit _enum: EnumLike[T]): ToPathParam[T] = new ToPathParam[T] {
    override def apply(t: T): String = _enum.getName(t)
  }

  implicit def toHeaderEnum[T](implicit _enum: EnumLike[T]): ToHeader[T] = new ToHeader[T] {
    override def apply(value: T): Option[String] = Some(_enum.getName(value))

    override def unapply(value: String): Option[T] = _enum.withNameOption(value)
  }

  trait KebsBaklavaEnumsUppercaseParams {
    implicit def toQueryParamEnum[T](implicit _enum: EnumLike[T]): ToQueryParam[T] = new ToQueryParam[T] {
      override def apply(t: T): Seq[String] = Seq(_enum.getName(t).toUpperCase)
    }

    implicit def toPathParamEnum[T](implicit _enum: EnumLike[T]): ToPathParam[T] = new ToPathParam[T] {
      override def apply(t: T): String = _enum.getName(t).toUpperCase
    }

    implicit def toHeaderEnum[T](implicit _enum: EnumLike[T]): ToHeader[T] = new ToHeader[T] {
      override def apply(value: T): Option[String] = Some(_enum.getName(value).toUpperCase)

      override def unapply(value: String): Option[T] = _enum.withNameUppercaseOnlyOption(value)
    }
  }

  trait KebsBaklavaEnumsLowercaseParams {
    implicit def toQueryParamEnum[T](implicit _enum: EnumLike[T]): ToQueryParam[T] = new ToQueryParam[T] {
      override def apply(t: T): Seq[String] = Seq(_enum.getName(t).toLowerCase)
    }

    implicit def toPathParamEnum[T](implicit _enum: EnumLike[T]): ToPathParam[T] = new ToPathParam[T] {
      override def apply(t: T): String = _enum.getName(t).toLowerCase
    }

    implicit def toHeaderEnum[T](implicit _enum: EnumLike[T]): ToHeader[T] = new ToHeader[T] {
      override def apply(value: T): Option[String] = Some(_enum.getName(value).toLowerCase)

      override def unapply(value: String): Option[T] = _enum.withNameLowercaseOnlyOption(value)
    }
  }
}

trait KebsBaklavaValueEnumsParams {
  implicit def toQueryParamValueEnum[V, E <: ValueEnumLikeEntry[V]](implicit
      @unused valueEnumLike: ValueEnumLike[V, E],
      tsm: ToQueryParam[V]
  ): ToQueryParam[E] = new ToQueryParam[E] {
    override def apply(e: E): Seq[String] = tsm(e.value)
  }

  implicit def toPathParamValueEnum[V, E <: ValueEnumLikeEntry[V]](implicit
      @unused valueEnumLike: ValueEnumLike[V, E],
      tsm: ToPathParam[V]
  ): ToPathParam[E] = new ToPathParam[E] {
    override def apply(e: E): String = tsm(e.value)
  }

  implicit def toHeaderValueEnum[V, E <: ValueEnumLikeEntry[V]](implicit
      valueEnumLike: ValueEnumLike[V, E],
      tsm: ToHeader[V]
  ): ToHeader[E] = new ToHeader[E] {
    override def apply(e: E): Option[String] = tsm(e.value)

    override def unapply(value: String): Option[E] = tsm.unapply(value).flatMap(valueEnumLike.withValueOption)
  }
}
