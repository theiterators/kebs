package pl.iterators.kebs.matchers

import akka.http.scaladsl.server.PathMatcher1
import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.macros.CaseClass1Rep
import pl.iterators.kebs.macros.enums.EnumOf
import scala.reflect.Enum

trait KebsMatchers {
  extension[U](segment: PathMatcher1[U]) {
    def as[T](using rep: CaseClass1Rep[T, U]): PathMatcher1[T] = segment.map(rep.apply)
  }

  extension[Source](segment: PathMatcher1[Source]) {
    def to[Type](using ico: InstanceConverter[Type, Source]): PathMatcher1[Type] = segment.map(ico.decode)
  }

  object EnumSegment {
    def as[T <: Enum](using e: EnumOf[T]): PathMatcher1[T] = {
      Segment.map(s => e.`enum`.values.find(_.toString.toLowerCase == s.toLowerCase).getOrElse(throw new IllegalArgumentException(s"enum case not found: $s")))
    }
  }
}
