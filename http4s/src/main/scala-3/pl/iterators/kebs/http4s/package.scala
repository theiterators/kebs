package pl.iterators.kebs.http4s

import scala.util.Try
import scala.reflect.Enum
import pl.iterators.kebs.macros.ValueClassLike
import org.http4s._
import pl.iterators.kebs.instances.InstanceConverter
import java.util.UUID
import pl.iterators.kebs.enums.EnumLike

protected class PathVar[A](cast: String => Try[A]) {
  def unapply(str: String): Option[A] =
    if (str.nonEmpty)
      cast(str).toOption
    else
      None
}

object WrappedString {
  def apply[T](using rep: ValueClassLike[T, String]) = new PathVar[T](str => Try(rep.apply(str)))
}

object InstanceString {
  def apply[T](using rep: InstanceConverter[T, String]) = new PathVar[T](str => Try(rep.decode(str)))
}

object EnumString {
  def apply[T <: Enum](using e: EnumLike[T]) = new PathVar[T](str => Try(e.values.find(_.toString.toUpperCase == str.toUpperCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))))
}

object WrappedInt {
  def apply[T](using rep: ValueClassLike[T, Int]) = new PathVar[T](str => Try(rep.apply(str.toInt)))
}

object InstanceInt {
  def apply[T](using rep: InstanceConverter[T, Int]) = new PathVar[T](str => Try(rep.decode(str.toInt)))
}

object WrappedLong {
  def apply[T](using rep: ValueClassLike[T, Long]) = new PathVar[T](str => Try(rep.apply(str.toLong)))
}

object InstanceLong {
  def apply[T](using rep: InstanceConverter[T, Long]) = new PathVar[T](str => Try(rep.decode(str.toLong)))
}

object WrappedUUID {
  def apply[T](using rep: ValueClassLike[T, UUID]) = new PathVar[T](str => Try(rep.apply(UUID.fromString(str))))
}

object InstanceUUID {
  def apply[T](using rep: InstanceConverter[T, UUID]) = new PathVar[T](str => Try(rep.decode(UUID.fromString(str))))
}

given[T, U](using rep: ValueClassLike[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] = qpd.emap(u => Try(rep.apply(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
given[T, U](using rep: InstanceConverter[T, U], qpd: QueryParamDecoder[U]): QueryParamDecoder[T] = qpd.emap(u => Try(rep.decode(u)).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
given[E <: Enum](using e: EnumLike[E]): QueryParamDecoder[E] = QueryParamDecoder[String].emap(str => Try(e.values.find(_.toString.toUpperCase == str.toUpperCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $str"))).toEither.left.map(t => ParseFailure(t.getMessage, t.getMessage)))
