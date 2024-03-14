package pl.iterators.kebs.instances.time

import YearMonthString.{YearMonthFormat, formatter}
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.YearMonth
import java.time.format.DateTimeFormatter

trait YearMonthString {
  implicit val yearMonthFormatter: InstanceConverter[YearMonth, String] =
    InstanceConverter[YearMonth, String](_.format(formatter), YearMonth.parse, Some(YearMonthFormat))
}
object YearMonthString {
  private val formatter = DateTimeFormatter.ofPattern("uuuu-MM")

  private[instances] val YearMonthFormat = "ISO-8601 standard format e.g. 2011-12"
}
