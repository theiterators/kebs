package pl.iterators.kebs.doobie.enums

import doobie.Meta
import scala.reflect.ClassTag

import pl.iterators.kebs.core.enums.EnumLike

trait KebsEnums {
  inline implicit def enumMeta[E](using e: EnumLike[E]): Meta[E] = Meta.StringMeta.imap(e.valueOf)(_.toString)
  inline implicit def enumArrayMeta[E](using e: EnumLike[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] =
    m.imap(_.map(e.valueOf))(_.map(_.toString))
  inline implicit def enumOptionArrayMeta[E](using
      e: EnumLike[E],
      m: Meta[Array[Option[String]]],
      ct: ClassTag[Option[E]]
  ): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.valueOf)))(_.map(_.map(_.toString)))

  trait Uppercase {
    inline implicit def enumUppercaseMeta[E](using e: EnumLike[E]): Meta[E] = Meta.StringMeta.imap(s =>
      e.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s"))
    )(_.toString.toUpperCase)
    inline implicit def enumUppercaseArrayMeta[E](using e: EnumLike[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(
      _.map(s => e.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))
    )(_.map(_.toString.toUpperCase))
    inline implicit def enumUppercaseOptionArrayMeta[E](using
        e: EnumLike[E],
        m: Meta[Array[Option[String]]],
        ct: ClassTag[Option[E]]
    ): Meta[Array[Option[E]]] = m.imap(
      _.map(
        _.map(s => e.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))
      )
    )(_.map(_.map(_.toString.toUpperCase)))
  }

  trait Lowercase {
    inline implicit def enumLowercaseMeta[E](using e: EnumLike[E]): Meta[E] = Meta.StringMeta.imap(s =>
      e.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s"))
    )(_.toString.toLowerCase)
    inline implicit def enumLowercaseMeta[E](using e: EnumLike[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(
      _.map(s => e.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))
    )(_.map(_.toString.toLowerCase))
    inline implicit def enumLowercaseOptionArrayMeta[E](using
        e: EnumLike[E],
        m: Meta[Array[Option[String]]],
        ct: ClassTag[Option[E]]
    ): Meta[Array[Option[E]]] = m.imap(
      _.map(
        _.map(s => e.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))
      )
    )(_.map(_.map(_.toString.toLowerCase)))
  }
}
