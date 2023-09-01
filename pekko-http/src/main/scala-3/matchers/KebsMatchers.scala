package pl.iterators.kebs.matchers

import org.apache.pekko.http.scaladsl.server.PathMatcher1
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.ValueClassLike
import pl.iterators.kebs.macros.enums.EnumOf
import org.apache.pekko.stream.Materializer
import scala.reflect.Enum

import scala.language.implicitConversions

trait KebsMatchers extends org.apache.pekko.http.scaladsl.server.PathMatchers {

  implicit class SegmentIsomorphism[U](segment: PathMatcher1[U]) {
    def as[T](implicit rep: ValueClassLike[T, U]): PathMatcher1[T] = segment.map(rep.apply)
  }

  implicit class SegmentConversion[Source](segment: PathMatcher1[Source]) {
    def to[Type](implicit ico: InstanceConverter[Type, Source]): PathMatcher1[Type] = segment.map(ico.decode)
  }

  object EnumSegment {
    def as[T <: Enum](using e: EnumOf[T]): PathMatcher1[T] = {
      Segment.map(s => e.`enum`.values.find(_.toString().toLowerCase() == s.toLowerCase()).getOrElse(throw new IllegalArgumentException(s"""Invalid value '$s'. Expected one of: ${e.`enum`.values.mkString(", ")}""")))
    }
  }
}
