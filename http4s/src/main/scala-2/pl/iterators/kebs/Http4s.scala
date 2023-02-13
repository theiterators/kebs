package pl.iterators.kebs

import enumeratum.EnumEntry

import scala.util.Try
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.macros.enums.EnumOf
import org.http4s._
import pl.iterators.kebs.converters.InstanceConverter

import java.util.UUID

trait Http4s {
  protected class PathVar[A](cast: String => Try[A]) {
    def unapply(str: String): Option[A] =
      if (str.nonEmpty)
        cast(str).toOption
      else
        None
  }

  object WrappedString {
    def apply[T](implicit rep: CaseClass1Rep[T, String]) = new PathVar[T](str => Try(rep.apply(str)))
  }

  object InstanceString {
    def apply[T](implicit rep: InstanceConverter[T, String]) = new PathVar[T](str => Try(rep.decode(str)))
  }

  object EnumString {
    def apply[T <: EnumEntry](implicit e: EnumOf[T]) = new PathVar[T](str => Try(e.`enum`.values.find(_.toString.toUpperCase == str.toUpperCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))))
  }

  object WrappedInt {
    def apply[T](implicit rep: CaseClass1Rep[T, Int]) = new PathVar[T](str => Try(rep.apply(str.toInt)))
  }

  object InstanceInt {
    def apply[T](implicit rep: InstanceConverter[T, Int]) = new PathVar[T](str => Try(rep.decode(str.toInt)))
  }

  object WrappedLong {
    def apply[T](implicit rep: CaseClass1Rep[T, Long]) = new PathVar[T](str => Try(rep.apply(str.toLong)))
  }

  object InstanceLong {
    def apply[T](implicit rep: InstanceConverter[T, Long]) = new PathVar[T](str => Try(rep.decode(str.toLong)))
  }

  object WrappedUUID {
    def apply[T](implicit rep: CaseClass1Rep[T, UUID]) = new PathVar[T](str => Try(rep.apply(UUID.fromString(str))))
  }

  object InstanceUUID {
    def apply[T](implicit rep: InstanceConverter[T, UUID]) = new PathVar[T](str => Try(rep.decode(UUID.fromString(str))))
  }

  implicit def cc1RepQueryParamDecoder[T, U](implicit rep: CaseClass1Rep[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] = qpd.emap(u => Try(rep.apply(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
  implicit def instanceConverterQueryParamDecoder[T, U](implicit rep: InstanceConverter[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] = qpd.emap(u => Try(rep.decode(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
  implicit def enumQueryParamDecoder[E <: EnumEntry](implicit e: EnumOf[E]): QueryParamDecoder[E] = QueryParamDecoder[String].emap(str => Try(e.`enum`.values.find(_.toString.toUpperCase == str.toUpperCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
}
