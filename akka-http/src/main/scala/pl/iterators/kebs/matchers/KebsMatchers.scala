package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.{PathMatcher1, PathMatchers}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.tagged._

import scala.reflect.ClassTag

trait KebsMatchers extends PathMatchers {
  implicit class CustomSegment(segment: PathMatcher1[String]) {
    def asString[T](implicit rep: CaseClass1Rep[T, String]): PathMatcher1[T] =
      segment.map(rep.apply)

    def asInt[T](implicit rep: CaseClass1Rep[T, Int]): PathMatcher1[T] =
      segment.map(str => rep.apply(str.toInt))

    def asLong[T](implicit rep: CaseClass1Rep[T, Long]): PathMatcher1[T] =
      segment.map(str => rep.apply(str.toLong))

    def asEnum[T <: EnumEntry: ClassTag](e: Enum[T]): PathMatcher1[T] =
      segment.map(e.withNameInsensitive)

    def asStringT[T]: PathMatcher1[String @@ T] =
      Segment.map(str => str.taggedWith[T])

    def asIntT[T]: PathMatcher1[Int @@ T] =
      Segment.map(str => str.toInt.taggedWith[T])

    def asLongT[T]: PathMatcher1[Long @@ T] =
      Segment.map(str => str.toLong.taggedWith[T])
  }
}
