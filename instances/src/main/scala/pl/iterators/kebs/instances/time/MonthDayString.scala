package pl.iterators.kebs.instances.time

import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.instances.time.MonthDayString.{MonthDayFormat, formatter}

import java.time.MonthDay
import java.time.format.DateTimeFormatter

trait MonthDayString {
  implicit val monthDayFormatter: InstanceConverter[MonthDay, String] =
    InstanceConverter[MonthDay, String](_.format(formatter), MonthDay.parse(_, formatter), Some(MonthDayFormat))
}
object MonthDayString {
  private val formatter = DateTimeFormatter.ofPattern("--MM-dd")

  private[instances] val MonthDayFormat = "ISO-8601 standard format e.g. --12-03"
}
