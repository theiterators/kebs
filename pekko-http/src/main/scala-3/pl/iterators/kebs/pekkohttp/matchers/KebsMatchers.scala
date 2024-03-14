package pl.iterators.kebs.pekkohttp.matchers

import org.apache.pekko.http.scaladsl.server.PathMatcher1
import pl.iterators.kebs.core.macros.ValueClassLike
import org.apache.pekko.stream.Materializer
import scala.reflect.Enum

import pl.iterators.kebs.core.enums.EnumLike
import pl.iterators.kebs.core.instances.InstanceConverter

import scala.language.implicitConversions

trait KebsMatchers extends org.apache.pekko.http.scaladsl.server.PathMatchers {

  implicit class SegmentIsomorphism[U](segment: PathMatcher1[U]) {
    def as[T](implicit rep: ValueClassLike[T, U]): PathMatcher1[T] = segment.map(rep.apply)
  }

  implicit class SegmentConversion[Source](segment: PathMatcher1[Source]) {
    def to[Type](implicit ico: InstanceConverter[Type, Source]): PathMatcher1[Type] = segment.map(ico.decode)
  }

  object EnumSegment {
    def as[T <: Enum](using e: EnumLike[T]): PathMatcher1[T] = {
      Segment.map(s => e.values.find(_.toString().toLowerCase() == s.toLowerCase()).getOrElse(throw new IllegalArgumentException(s"""Invalid value '$s'. Expected one of: ${e.values.mkString(", ")}""")))
    }
  }
}
