package pl.iterators.kebs.instances.time

import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.time.YearString.YearFormat

import java.time.Year

trait YearString {
  implicit val yearFormatter: InstanceConverter[Year, String] =
    InstanceConverter[Year, String](_.toString, Year.parse, Some(YearFormat))
}
object YearString {
  private[instances] val YearFormat = "ISO-8601 standard format e.g. 2007"
}
