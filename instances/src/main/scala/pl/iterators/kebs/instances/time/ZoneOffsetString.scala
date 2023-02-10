package pl.iterators.kebs.instances.time

import pl.iterators.kebs.core.InstanceConverter
import pl.iterators.kebs.instances.time.ZoneOffsetString.ZoneOffsetFormat

import java.time.ZoneOffset

trait ZoneOffsetString {
  implicit val zoneOffsetFormatter: InstanceConverter[ZoneOffset, String] =
    InstanceConverter[ZoneOffset, String](_.toString, ZoneOffset.of, Some(ZoneOffsetFormat))
}
object ZoneOffsetString {
  private[instances] val ZoneOffsetFormat = "ISO-8601 standard format e.g. +01:00"
}
