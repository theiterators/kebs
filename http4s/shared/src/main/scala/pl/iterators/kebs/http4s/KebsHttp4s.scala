package pl.iterators.kebs.http4s

import org.http4s._
import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

import java.util.UUID
import scala.util.Try

trait KebsHttp4s {
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
    def apply[T](implicit e: EnumLike[T], naming: EnumNaming) = {
      val named = naming.applyTo(e)
      new PathVar[T](str => Try(naming.lookup(named, str).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))))
    }
  }

  object ValueEnumString {
    def apply[T <: ValueEnumLikeEntry[String]](implicit e: ValueEnumLike[String, T]) = new PathVar[T](str => Try(e.withValue(str)))
  }

  object ValueEnumInt {
    def apply[T <: ValueEnumLikeEntry[Int]](implicit e: ValueEnumLike[Int, T]) = new PathVar[T](str => Try(e.withValue(str.toInt)))
  }

  object ValueEnumLong {
    def apply[T <: ValueEnumLikeEntry[Long]](implicit e: ValueEnumLike[Long, T]) = new PathVar[T](str => Try(e.withValue(str.toLong)))
  }

  object ValueEnumShort {
    def apply[T <: ValueEnumLikeEntry[Short]](implicit e: ValueEnumLike[Short, T]) = new PathVar[T](str => Try(e.withValue(str.toShort)))
  }

  object ValueEnumByte {
    def apply[T <: ValueEnumLikeEntry[Byte]](implicit e: ValueEnumLike[Byte, T]) = new PathVar[T](str => Try(e.withValue(str.toByte)))
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

  implicit def vcLikeQueryParamDecoder[T, U](implicit rep: ValueClassLike[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] =
    qpd.emap(u => Try(rep.apply(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
  implicit def instanceConverterQueryParamDecoder[T, U](implicit
      rep: InstanceConverter[T, U],
      qpd: QueryParamDecoder[U]
  ): QueryParamDecoder[T] = qpd.emap(u => Try(rep.decode(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
  implicit def enumQueryParamDecoder[E](implicit e: EnumLike[E], naming: EnumNaming): QueryParamDecoder[E] = {
    val named = naming.applyTo(e)
    QueryParamDecoder[String].emap(str =>
      naming.lookup(named, str).toRight(ParseFailure(s"enum case not found: $str", s"enum case not found: $str"))
    )
  }
  implicit def valueEnumQueryParamDecoder[V, E <: ValueEnumLikeEntry[V]](implicit
      e: ValueEnumLike[V, E],
      qpd: QueryParamDecoder[V]
  ): QueryParamDecoder[E] = qpd.emap { value =>
    val message = s"$value is not a member of ${e.values.map(_.value).mkString(", ")}"
    e.withValueOption(value).toRight(ParseFailure(message, message))
  }
}
