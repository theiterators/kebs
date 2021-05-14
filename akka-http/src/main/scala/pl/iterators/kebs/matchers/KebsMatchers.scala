package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.{PathMatcher1, PathMatchers}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.tagged._

import scala.reflect.ClassTag

trait KebsMatchers extends PathMatchers {
  implicit class CustomSegment(segment: PathMatcher1[String]) {
    def as[T](implicit rep: CaseClass1Rep[T, String]): PathMatcher1[T] =
      segment.map(rep.apply)

    def asEnum[T <: EnumEntry: ClassTag](e: Enum[T]): PathMatcher1[T] =
      segment.map(e.withNameInsensitive)

    def asTagged[T]: PathMatcher1[String @@ T] =
      segment.map(_.taggedWith[T])
  }

  implicit class IntSegment(segment: PathMatcher1[Int]) {
    def as[T](implicit rep: CaseClass1Rep[T, Int]): PathMatcher1[T] =
      segment.map(rep.apply)

    def asTagged[T]: PathMatcher1[Int @@ T] =
      segment.map(_.taggedWith[T])
  }

  implicit class LongSegment(segment: PathMatcher1[Long]) {
    def as[T](implicit rep: CaseClass1Rep[T, Long]): PathMatcher1[T] =
      segment.map(rep.apply)

    def asTagged[T]: PathMatcher1[Long @@ T] =
      segment.map(_.taggedWith[T])
  }
}
