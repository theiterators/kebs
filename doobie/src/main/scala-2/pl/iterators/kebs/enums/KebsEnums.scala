package pl.iterators.kebs.enums

import doobie.Meta
import enumeratum.EnumEntry
import pl.iterators.kebs.macros.enums.EnumOf

import scala.reflect.ClassTag

trait KebsEnums {
  implicit def enumMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[String]): Meta[E] = m.imap(e.`enum`.withName)(_.toString)
  implicit def enumArrayMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(e.`enum`.withName))(_.map(_.toString))
  implicit def enumOptionArrayMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[Array[Option[String]]], cte: ClassTag[Option[E]]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.`enum`.withName)))(_.map(_.map(_.toString)))

  trait Uppercase {
    implicit def enumUppercaseMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[String]): Meta[E] = m.imap(e.`enum`.withNameUppercaseOnly)(_.toString.toUpperCase)
    implicit def enumUppercaseArrayMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(e.`enum`.withNameUppercaseOnly))(_.map(_.toString.toUpperCase))
    implicit def enumUppercaseOptionArrayMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[Array[Option[String]]], cte: ClassTag[E]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.`enum`.withNameUppercaseOnly)))(_.map(_.map(_.toString.toUpperCase)))
  }

  trait Lowercase {
    implicit def enumLowercaseMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[String]): Meta[E] = m.imap(e.`enum`.withNameLowercaseOnly)(_.toString.toLowerCase)
    implicit def enumLowercaseArrayMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[Array[String]], cte: ClassTag[E]): Meta[Array[E]] = m.imap(_.map(e.`enum`.withNameLowercaseOnly))(_.map(_.toString.toLowerCase))
    implicit def enumLowercaseOptionArrayMeta[E <: EnumEntry](implicit e: EnumOf[E], m: Meta[Array[Option[String]]], cte: ClassTag[E]): Meta[Array[Option[E]]] = m.imap(_.map(_.map(e.`enum`.withNameLowercaseOnly)))(_.map(_.map(_.toString.toLowerCase)))
  }
}

object KebsEnums extends KebsEnums