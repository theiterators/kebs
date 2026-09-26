package pl.iterators.kebs.baklava.params.enums

import pl.iterators.baklava.{ToHeader, ToPathParam, ToQueryParam}
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

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
  implicit def toQueryParamValueEnum[T, V <: ValueEnumLikeEntry[T]](implicit
      valueEnumLike: ValueEnumLike[T, V],
      tsm: ToQueryParam[V]
  ): ToQueryParam[T] = new ToQueryParam[T] {
    override def apply(t: T): Seq[String] = tsm(valueEnumLike.valueOf(t))
  }

  implicit def toPathParamValueEnum[T, V <: ValueEnumLikeEntry[T]](implicit
      valueEnumLike: ValueEnumLike[T, V],
      tsm: ToPathParam[V]
  ): ToPathParam[T] = new ToPathParam[T] {
    override def apply(t: T): String = tsm(valueEnumLike.valueOf(t))
  }

  implicit def toHeaderValueEnum[T, V <: ValueEnumLikeEntry[T]](implicit
      valueEnumLike: ValueEnumLike[T, V],
      tsm: ToHeader[V]
  ): ToHeader[T] = new ToHeader[T] {
    override def apply(value: T): Option[String] = tsm(valueEnumLike.valueOf(value))

    override def unapply(value: String): Option[T] =
      tsm.unapply(value).flatMap(v => valueEnumLike.getValuesToEntriesMap.toList.find(_._2 == v).map(_._1))
  }
}
