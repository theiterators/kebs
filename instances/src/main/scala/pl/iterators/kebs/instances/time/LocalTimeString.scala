package pl.iterators.kebs.instances.time

import LocalTimeString.{LocalTimeFormat, formatter}
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.LocalTime
import java.time.format.DateTimeFormatter

trait LocalTimeString {
  implicit val localTimeFormatter: InstanceConverter[LocalTime, String] =
    InstanceConverter[LocalTime, String](_.format(formatter), LocalTime.parse(_, formatter), Some(LocalTimeFormat))
}
object LocalTimeString {
  private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

  private[instances] val LocalTimeFormat = "ISO-8601 standard format e.g. 10:15:30"
}
