package pl.iterators.kebs.instances.time

import InstantString.InstantFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.Instant

trait InstantString {
  implicit val instantFormatter: InstanceConverter[Instant, String] =
    InstanceConverter[Instant, String](_.toString, Instant.parse, Some(InstantFormat))
}
object InstantString {
  private[instances] val InstantFormat = "ISO-8601 standard format e.g. 2007-12-03T10:15:30.00Z"
}
