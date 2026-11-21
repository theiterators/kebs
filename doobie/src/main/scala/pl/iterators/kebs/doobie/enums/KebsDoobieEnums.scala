package pl.iterators.kebs.doobie.enums

import doobie.Meta
import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}

import scala.reflect.ClassTag

trait KebsDoobieEnums {
  implicit def enumMeta[E](implicit e: EnumLike[E], m: Meta[String]): Meta[E] = m.imap(e.withName)(e.getName)
  implicit def enumArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] =
    m.imap(_.map(e.withName))(_.map(e.getName))
  implicit def enumOptionArrayMeta[E](implicit
      e: EnumLike[E],
      m: Meta[Array[Option[String]]],
      cte: ClassTag[Option[E]]
  ): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.withName)))(_.map(_.map(e.getName)))

  trait KebsDoobieEnumsUppercase {
    implicit def enumUppercaseMeta[E](implicit e: EnumLike[E], m: Meta[String]): Meta[E] =
      m.imap(e.withNameUppercaseOnly)(e.getName(_).toUpperCase)
    implicit def enumUppercaseArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] =
      m.imap(_.map(e.withNameUppercaseOnly))(_.map(e.getName(_).toUpperCase))
    implicit def enumUppercaseOptionArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[Option[String]]]): Meta[Array[Option[E]]] =
      m.imap(_.map(_.map(e.withNameUppercaseOnly)))(_.map(_.map(e.getName(_).toUpperCase)))
  }

  trait KebsDoobieEnumsLowercase {
    implicit def enumLowercaseMeta[E](implicit e: EnumLike[E], m: Meta[String]): Meta[E] =
      m.imap(e.withNameLowercaseOnly)(e.getName(_).toLowerCase)
    implicit def enumLowercaseArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] =
      m.imap(_.map(e.withNameLowercaseOnly))(_.map(e.getName(_).toLowerCase))
    implicit def enumLowercaseOptionArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[Option[String]]]): Meta[Array[Option[E]]] =
      m.imap(_.map(_.map(e.withNameLowercaseOnly)))(_.map(_.map(e.getName(_).toLowerCase)))
  }
}

trait KebsDoobieValueEnums {
  implicit def valueEnumMeta[V, E <: ValueEnumLikeEntry[V]](implicit e: ValueEnumLike[V, E], m: Meta[V]): Meta[E] =
    m.imap(e.withValue)(_.value)

  implicit def valueEnumArrayMeta[V, E <: ValueEnumLikeEntry[V]](implicit
      e: ValueEnumLike[V, E],
      m: Meta[Array[V]],
      cte: ClassTag[E],
      ctv: ClassTag[V]
  ): Meta[Array[E]] = m.imap(_.map(e.withValue))(_.map(_.value))

  implicit def valueEnumOptionArrayMeta[V, E <: ValueEnumLikeEntry[V]](implicit
      e: ValueEnumLike[V, E],
      m: Meta[Array[Option[V]]],
      cte: ClassTag[Option[E]],
      ctv: ClassTag[V]
  ): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.withValue)))(_.map(_.map(_.value)))
}
