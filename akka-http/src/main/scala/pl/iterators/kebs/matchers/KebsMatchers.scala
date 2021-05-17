package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.{PathMatcher1, PathMatchers}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.macros.CaseClass1Rep

import java.util.UUID

trait KebsMatchers extends PathMatchers {

  abstract class CustomSegment[U](segment: PathMatcher1[U]) {
    def as[T](implicit rep: CaseClass1Rep[T, U]): PathMatcher1[T] = segment.map(rep.apply)
  }

  implicit class StringSegment(segment: PathMatcher1[String]) extends CustomSegment[String](segment)
  implicit class IntSegment(segment: PathMatcher1[Int])       extends CustomSegment[Int](segment)
  implicit class LongSegment(segment: PathMatcher1[Long])     extends CustomSegment[Long](segment)
  implicit class DoubleSegment(segment: PathMatcher1[Double]) extends CustomSegment[Double](segment)
  implicit class UUIDSegment(segment: PathMatcher1[UUID])     extends CustomSegment[UUID](segment)

  object EnumSegment {
    def as[T <: EnumEntry: Enum]: PathMatcher1[T] = {
      val enumCompanion = implicitly[Enum[T]]
      Segment.map(enumCompanion.withNameInsensitive)
    }
  }
}
