package pl.iterators.kebs.instances.time

import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.instances.time.InstantString.InstantFormat

import java.time.Instant

trait InstantString {
  implicit val instantFormatter: InstanceConverter[Instant, String] =
    InstanceConverter[Instant, String](_.toString, Instant.parse, Some(InstantFormat))
}
object InstantString {
  private[instances] val InstantFormat = "ISO-8601 standard format e.g. 2007-12-03T10:15:30.00Z"
}
