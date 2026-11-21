package pl.iterators.kebs.baklava.params.enums

import pl.iterators.baklava.{ToHeader, ToPathParam, ToQueryParam}
import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}

import scala.annotation.unused

trait KebsBaklavaEnumsParams {
  implicit def toQueryParamEnum[T](implicit _enum: EnumLike[T], naming: EnumNaming): ToQueryParam[T] = new ToQueryParam[T] {
    private val named                     = naming.applyTo(_enum)
    override def apply(t: T): Seq[String] = Seq(named.getName(t))
  }

  implicit def toPathParamEnum[T](implicit _enum: EnumLike[T], naming: EnumNaming): ToPathParam[T] = new ToPathParam[T] {
    private val named                = naming.applyTo(_enum)
    override def apply(t: T): String = named.getName(t)
  }

  implicit def toHeaderEnum[T](implicit _enum: EnumLike[T], naming: EnumNaming): ToHeader[T] = new ToHeader[T] {
    private val named                              = naming.applyTo(_enum)
    override def apply(value: T): Option[String]   = Some(named.getName(value))
    override def unapply(value: String): Option[T] = naming.lookup(named, value)
  }

  trait KebsBaklavaEnumsUppercaseParams {
    implicit def toQueryParamEnum[T](implicit _enum: EnumLike[T]): ToQueryParam[T] =
      KebsBaklavaEnumsParams.this.toQueryParamEnum(_enum, EnumNaming.uppercase)
    implicit def toPathParamEnum[T](implicit _enum: EnumLike[T]): ToPathParam[T] =
      KebsBaklavaEnumsParams.this.toPathParamEnum(_enum, EnumNaming.uppercase)
    implicit def toHeaderEnum[T](implicit _enum: EnumLike[T]): ToHeader[T] =
      KebsBaklavaEnumsParams.this.toHeaderEnum(_enum, EnumNaming.uppercase)
  }

  trait KebsBaklavaEnumsLowercaseParams {
    implicit def toQueryParamEnum[T](implicit _enum: EnumLike[T]): ToQueryParam[T] =
      KebsBaklavaEnumsParams.this.toQueryParamEnum(_enum, EnumNaming.lowercase)
    implicit def toPathParamEnum[T](implicit _enum: EnumLike[T]): ToPathParam[T] =
      KebsBaklavaEnumsParams.this.toPathParamEnum(_enum, EnumNaming.lowercase)
    implicit def toHeaderEnum[T](implicit _enum: EnumLike[T]): ToHeader[T] =
      KebsBaklavaEnumsParams.this.toHeaderEnum(_enum, EnumNaming.lowercase)
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
