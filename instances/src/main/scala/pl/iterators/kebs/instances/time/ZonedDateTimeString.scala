package pl.iterators.kebs.instances.time

import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.instances.time.ZonedDateTimeString.{ZonedDateTimeFormat, formatter}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

trait ZonedDateTimeString {
  implicit val zonedDateTimeFormatter: InstanceConverter[ZonedDateTime, String] =
    InstanceConverter[ZonedDateTime, String](_.format(formatter), ZonedDateTime.parse(_, formatter), Some(ZonedDateTimeFormat))
}
object ZonedDateTimeString {
  private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

  private[instances] val ZonedDateTimeFormat = "ISO-8601 standard format extended with zone e.g. 2011-12-03T10:15:30+01:00[Europe/Warsaw]"
}
