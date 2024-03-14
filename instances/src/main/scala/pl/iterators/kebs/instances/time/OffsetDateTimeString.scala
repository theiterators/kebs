package pl.iterators.kebs.instances.time

import OffsetDateTimeString.{OffsetDateTimeFormat, formatter}
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

trait OffsetDateTimeString {
  implicit val offsetDateTimeFormatter: InstanceConverter[OffsetDateTime, String] =
    InstanceConverter[OffsetDateTime, String](_.format(formatter), OffsetDateTime.parse(_, formatter), Some(OffsetDateTimeFormat))
}
object OffsetDateTimeString {
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  private[instances] val OffsetDateTimeFormat = "ISO-8601 standard format e.g. 2011-12-03T10:15:30+01:00"
}
