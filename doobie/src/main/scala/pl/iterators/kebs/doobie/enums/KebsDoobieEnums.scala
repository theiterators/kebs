package pl.iterators.kebs.doobie.enums

import org.typelevel.doobie.Meta
import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}

import scala.reflect.ClassTag

trait KebsDoobieEnums {
  implicit def enumMeta[E](implicit e: EnumLike[E], naming: EnumNaming, m: Meta[String]): Meta[E] = {
    val named = naming.applyTo(e)
    m.imap(naming.lookupOrThrow(named, _))(named.getName)
  }
  implicit def enumArrayMeta[E](implicit
      e: EnumLike[E],
      naming: EnumNaming,
      m: Meta[Array[String]],
      cte: ClassTag[E]
  ): Meta[Array[E]] = {
    val named = naming.applyTo(e)
    m.imap(_.map(naming.lookupOrThrow(named, _)))(_.map(named.getName))
  }
  implicit def enumOptionArrayMeta[E](implicit
      e: EnumLike[E],
      naming: EnumNaming,
      m: Meta[Array[Option[String]]],
      cte: ClassTag[Option[E]]
  ): Meta[Array[Option[E]]] = {
    val named = naming.applyTo(e)
    m.imap(_.map(_.map(naming.lookupOrThrow(named, _))))(_.map(_.map(named.getName)))
  }

  trait KebsDoobieEnumsUppercase extends KebsDoobieEnums {
    implicit val kebsEnumNaming: EnumNaming = EnumNaming.uppercase
  }

  trait KebsDoobieEnumsLowercase extends KebsDoobieEnums {
    implicit val kebsEnumNaming: EnumNaming = EnumNaming.lowercase
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
