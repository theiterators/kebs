package pl.iterators.kebs.doobie.enums

import doobie.Meta
import scala.reflect.ClassTag
import scala.reflect.Enum

import pl.iterators.kebs.core.enums.EnumLike

trait KebsEnums {
  inline given enumMeta[E](using e: EnumLike[E]): Meta[E] = Meta.StringMeta.imap(e.valueOf)(_.toString)
  inline given enumArrayMeta[E](using e: EnumLike[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(e.valueOf))(_.map(_.toString))
  inline given enumOptionArrayMeta[E](using e: EnumLike[E], m: Meta[Array[Option[String]]], ct: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.valueOf)))(_.map(_.map(_.toString)))

  trait Uppercase {
    inline given enumUppercaseMeta[E](using e: EnumLike[E]): Meta[E] = Meta.StringMeta.imap(s => e.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))(_.toString.toUpperCase)
    inline given enumUppercaseArrayMeta[E](using e: EnumLike[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(s => e.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s"))))(_.map(_.toString.toUpperCase))
    inline given enumUppercaseOptionArrayMeta[E](using e: EnumLike[E], m: Meta[Array[Option[String]]], ct: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(s => e.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))))(_.map(_.map(_.toString.toUpperCase)))
  }

  trait Lowercase {
    inline given enumLowercaseMeta[E](using e: EnumLike[E]): Meta[E] = Meta.StringMeta.imap(s => e.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))(_.toString.toLowerCase)
    inline given enumLowercaseMeta[E](using e: EnumLike[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(s => e.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s"))))(_.map(_.toString.toLowerCase))
    inline given enumLowercaseOptionArrayMeta[E](using e: EnumLike[E], m: Meta[Array[Option[String]]], ct: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(s => e.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))))(_.map(_.map(_.toString.toLowerCase)))
  }
}