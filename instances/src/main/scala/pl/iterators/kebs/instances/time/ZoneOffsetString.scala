package pl.iterators.kebs.instances.time

import ZoneOffsetString.ZoneOffsetFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.ZoneOffset

trait ZoneOffsetString {
  implicit val zoneOffsetFormatter: InstanceConverter[ZoneOffset, String] =
    InstanceConverter[ZoneOffset, String](_.toString, ZoneOffset.of, Some(ZoneOffsetFormat))
}
object ZoneOffsetString {
  private[instances] val ZoneOffsetFormat = "ISO-8601 standard format e.g. +01:00"
}
