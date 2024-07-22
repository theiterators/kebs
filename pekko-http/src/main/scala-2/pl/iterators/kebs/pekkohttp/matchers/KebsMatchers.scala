package pl.iterators.kebs.pekkohttp.matchers

import org.apache.pekko.http.scaladsl.server.{PathMatcher1, PathMatchers}
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.enums._

trait KebsMatchers extends PathMatchers {

  implicit class SegmentIsomorphism[U](segment: PathMatcher1[U]) {
    def as[T](implicit rep: ValueClassLike[T, U]): PathMatcher1[T] = segment.map(rep.apply)
  }

  implicit class SegmentConversion[Source](segment: PathMatcher1[Source]) {
    def to[Type](implicit ico: InstanceConverter[Type, Source]): PathMatcher1[Type] = segment.map(ico.decode)
  }

  object EnumSegment {
    def as[T](implicit e: EnumLike[T]): PathMatcher1[T] = {
      Segment.map(e.withNameIgnoreCase)
    }
  }
}
