package pl.iterators.kebs.http4sstir.matchers

import pl.iterators.stir.server.PathMatcher1
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}
import pl.iterators.kebs.core.enums._

trait KebsMatchers extends pl.iterators.stir.server.PathMatchers with CaseClass1ToValueClass {

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
