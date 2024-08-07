package pl.iterators.kebs.pekkohttp.matchers

import org.apache.pekko.http.scaladsl.server.PathMatcher1
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.enums._

trait KebsPekkoHttpMatchers extends org.apache.pekko.http.scaladsl.server.PathMatchers {
  implicit class SegmentIsomorphism[U](segment: PathMatcher1[U]) {
    def as[T](implicit rep: ValueClassLike[T, U]): PathMatcher1[T]                                = segment.map(rep.apply)
    def asValueEnum[T <: ValueEnumLikeEntry[U]](implicit e: ValueEnumLike[U, T]): PathMatcher1[T] = segment.map(e.withValue)
  }

  implicit class SegmentConversion[Source](segment: PathMatcher1[Source]) {
    def to[Type](implicit ico: InstanceConverter[Type, Source]): PathMatcher1[Type] = segment.map(ico.decode)
  }

  implicit class SegmentEnumIsomorphism[U](segment: PathMatcher1[String]) {
    def asEnum[T](implicit e: EnumLike[T]): PathMatcher1[T] = segment.map(e.withNameIgnoreCase)
  }
}
