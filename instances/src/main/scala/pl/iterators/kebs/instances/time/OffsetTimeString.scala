package pl.iterators.kebs.instances.time

import OffsetTimeString.{OffsetTimeFormat, formatter}
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.OffsetTime
import java.time.format.DateTimeFormatter

trait OffsetTimeString {
  implicit val offsetTimeFormatter: InstanceConverter[OffsetTime, String] =
    InstanceConverter[OffsetTime, String](_.format(formatter), OffsetTime.parse(_, formatter), Some(OffsetTimeFormat))
}
object OffsetTimeString {
  private val formatter = DateTimeFormatter.ISO_OFFSET_TIME

  private[instances] val OffsetTimeFormat = "ISO-8601 standard format e.g. 10:15:30+01:00"
}
