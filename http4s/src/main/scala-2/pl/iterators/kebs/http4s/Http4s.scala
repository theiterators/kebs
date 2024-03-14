package pl.iterators.kebs.http4s

import org.http4s._
import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

import java.util.UUID
import scala.util.Try

trait Http4s extends CaseClass1ToValueClass {
  protected class PathVar[A](cast: String => Try[A]) {
    def unapply(str: String): Option[A] =
      if (str.nonEmpty)
        cast(str).toOption
      else
        None
  }

  object WrappedString {
    def apply[T](implicit rep: ValueClassLike[T, String]) = new PathVar[T](str => Try(rep.apply(str)))
  }

  object InstanceString {
    def apply[T](implicit rep: InstanceConverter[T, String]) = new PathVar[T](str => Try(rep.decode(str)))
  }

  object EnumString {
    def apply[T](implicit e: EnumLike[T]) = new PathVar[T](str => Try(e.values.find(_.toString.toUpperCase == str.toUpperCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))))
  }

  object WrappedInt {
    def apply[T](implicit rep: ValueClassLike[T, Int]) = new PathVar[T](str => Try(rep.apply(str.toInt)))
  }

  object InstanceInt {
    def apply[T](implicit rep: InstanceConverter[T, Int]) = new PathVar[T](str => Try(rep.decode(str.toInt)))
  }

  object WrappedLong {
    def apply[T](implicit rep: ValueClassLike[T, Long]) = new PathVar[T](str => Try(rep.apply(str.toLong)))
  }

  object InstanceLong {
    def apply[T](implicit rep: InstanceConverter[T, Long]) = new PathVar[T](str => Try(rep.decode(str.toLong)))
  }

  object WrappedUUID {
    def apply[T](implicit rep: ValueClassLike[T, UUID]) = new PathVar[T](str => Try(rep.apply(UUID.fromString(str))))
  }

  object InstanceUUID {
    def apply[T](implicit rep: InstanceConverter[T, UUID]) = new PathVar[T](str => Try(rep.decode(UUID.fromString(str))))
  }

  implicit def vcLikeQueryParamDecoder[T, U](implicit rep: ValueClassLike[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] = qpd.emap(u => Try(rep.apply(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
  implicit def instanceConverterQueryParamDecoder[T, U](implicit rep: InstanceConverter[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] = qpd.emap(u => Try(rep.decode(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
  implicit def enumQueryParamDecoder[E](implicit e: EnumLike[E]): QueryParamDecoder[E] = QueryParamDecoder[String].emap(str => Try(e.values.find(_.toString.toUpperCase == str.toUpperCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
}
