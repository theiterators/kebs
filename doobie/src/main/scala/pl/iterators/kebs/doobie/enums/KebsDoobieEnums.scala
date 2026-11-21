package pl.iterators.kebs.doobie.enums

import org.typelevel.doobie.Meta
import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}

import scala.reflect.ClassTag

trait KebsDoobieEnums {
  // mixing the base trait with a preset must not compile (it would silently pick one of them)
  protected def kebsEnumCodecs: Unit = ()

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

  trait KebsDoobieEnumsUppercase {
    protected def kebsEnumCodecs: Unit = ()

    implicit def enumUppercaseMeta[E](implicit e: EnumLike[E], m: Meta[String]): Meta[E] =
      enumMeta(e, EnumNaming.uppercase, m)
    implicit def enumUppercaseArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] =
      enumArrayMeta(e, EnumNaming.uppercase, m, cte)
    implicit def enumUppercaseOptionArrayMeta[E](implicit
        e: EnumLike[E],
        m: Meta[Array[Option[String]]],
        cte: ClassTag[Option[E]]
    ): Meta[Array[Option[E]]] =
      enumOptionArrayMeta(e, EnumNaming.uppercase, m, cte)
  }

  trait KebsDoobieEnumsLowercase {
    protected def kebsEnumCodecs: Unit = ()

    implicit def enumLowercaseMeta[E](implicit e: EnumLike[E], m: Meta[String]): Meta[E] =
      enumMeta(e, EnumNaming.lowercase, m)
    implicit def enumLowercaseArrayMeta[E](implicit e: EnumLike[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] =
      enumArrayMeta(e, EnumNaming.lowercase, m, cte)
    implicit def enumLowercaseOptionArrayMeta[E](implicit
        e: EnumLike[E],
        m: Meta[Array[Option[String]]],
        cte: ClassTag[Option[E]]
    ): Meta[Array[Option[E]]] =
      enumOptionArrayMeta(e, EnumNaming.lowercase, m, cte)
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
