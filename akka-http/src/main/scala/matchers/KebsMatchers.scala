package pl.iterators.kebs

import akka.http.scaladsl.server.{PathMatcher1, PathMatchers}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.CaseClass1Rep

import scala.language.implicitConversions

trait KebsMatchers extends PathMatchers {

  implicit class SegmentIsomorphism[U](segment: PathMatcher1[U]) {
    def as[T](implicit rep: CaseClass1Rep[T, U]): PathMatcher1[T] = segment.map(rep.apply)
  }

  implicit class SegmentConversion[Source](segment: PathMatcher1[Source]) {
    def to[Type](implicit ico: InstanceConverter[Type, Source]): PathMatcher1[Type] = segment.map(ico.decode)
  }

  object EnumSegment {
    def as[T <: EnumEntry: Enum]: PathMatcher1[T] = {
      val enumCompanion = implicitly[Enum[T]]
      Segment.map(enumCompanion.withNameInsensitive)
    }
  }
}
