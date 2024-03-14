package pl.iterators.kebs.instances.time

import LocalDateString.{LocalDateFormat, formatter}
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait LocalDateString {
  implicit val localDateFormatter: InstanceConverter[LocalDate, String] =
    InstanceConverter[LocalDate, String](_.format(formatter), LocalDate.parse(_, formatter), Some(LocalDateFormat))
}
object LocalDateString {
  private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

  private[instances] val LocalDateFormat = "ISO-8601 standard format e.g. 2007-12-03"
}
