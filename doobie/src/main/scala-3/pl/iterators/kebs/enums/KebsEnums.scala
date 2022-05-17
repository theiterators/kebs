package pl.iterators.kebs.enums

import doobie.Meta
import pl.iterators.kebs.macros.enums.{EnumOf, EnumLike}
import scala.reflect.ClassTag
import scala.reflect.Enum

trait KebsEnums {
  inline given enumMeta[E <: Enum](using e: EnumOf[E]): Meta[E] = Meta.StringMeta.imap(e.`enum`.valueOf)(_.toString)
  inline given enumArrayMeta[E <: Enum](using e: EnumOf[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(e.`enum`.valueOf))(_.map(_.toString))
  inline given enumOptionArrayMeta[E <: Enum](using e: EnumOf[E], m: Meta[Array[Option[String]]], ct: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.`enum`.valueOf)))(_.map(_.map(_.toString)))

  trait Uppercase {
    inline given enumUppercaseMeta[E <: Enum](using e: EnumOf[E]): Meta[E] = Meta.StringMeta.imap(s => e.`enum`.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))(_.toString.toUpperCase)
    inline given enumUppercaseArrayMeta[E <: Enum](using e: EnumOf[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(s => e.`enum`.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s"))))(_.map(_.toString.toUpperCase))
    inline given enumUppercaseOptionArrayMeta[E <: Enum](using e: EnumOf[E], m: Meta[Array[Option[String]]], ct: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(s => e.`enum`.values.find(_.toString.toUpperCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))))(_.map(_.map(_.toString.toUpperCase)))
  }

  trait Lowercase {
    inline given enumLowercaseMeta[E <: Enum](using e: EnumOf[E]): Meta[E] = Meta.StringMeta.imap(s => e.`enum`.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))(_.toString.toLowerCase)
    inline given enumLowercaseMeta[E <: Enum](using e: EnumOf[E], m: Meta[Array[String]], ct: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(s => e.`enum`.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s"))))(_.map(_.toString.toLowerCase))
    inline given enumLowercaseOptionArrayMeta[E <: Enum](using e: EnumOf[E], m: Meta[Array[Option[String]]], ct: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(s => e.`enum`.values.find(_.toString.toLowerCase == s).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))))(_.map(_.map(_.toString.toLowerCase)))
  }
}