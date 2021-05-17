package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.{PathMatcher1, PathMatchers}
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.macros.CaseClass1Rep

import java.util.UUID

trait KebsMatchers extends PathMatchers {

  implicit class CustomSegment(segment: PathMatcher1[String]) {
    def asString[T](implicit rep: CaseClass1Rep[T, String]): PathMatcher1[T] = segment.map(rep.apply)
    def asInt[T](implicit rep: CaseClass1Rep[T, Int]): PathMatcher1[T]       = segment.map(str => rep.apply(str.toInt))
    def asLong[T](implicit rep: CaseClass1Rep[T, Long]): PathMatcher1[T]     = segment.map(str => rep.apply(str.toLong))
    def asUUID[T](implicit rep: CaseClass1Rep[T, UUID]): PathMatcher1[T]     = segment.map(str => rep.apply(UUID.fromString(str)))

    def asEnum[T <: EnumEntry: Enum]: PathMatcher1[T] = {
      val enumCompanion = implicitly[Enum[T]]
      segment.map(enumCompanion.withNameInsensitive)
    }
  }
}
