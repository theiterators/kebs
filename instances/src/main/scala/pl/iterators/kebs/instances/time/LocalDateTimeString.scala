package pl.iterators.kebs.instances.time

import LocalDateTimeString.{LocalDateTimeFormat, formatter}
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait LocalDateTimeString {
  implicit val localDateTimeFormatter: InstanceConverter[LocalDateTime, String] =
    InstanceConverter[LocalDateTime, String](_.format(formatter), LocalDateTime.parse(_, formatter), Some(LocalDateTimeFormat))
}
object LocalDateTimeString {
  private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  private[instances] val LocalDateTimeFormat = "ISO-8601 standard format e.g. 2007-12-03T10:15:30"
}
