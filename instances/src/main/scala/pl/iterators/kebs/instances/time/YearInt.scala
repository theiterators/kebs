package pl.iterators.kebs.instances.time

import YearInt.YearFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.Year

trait YearInt {
  implicit val yearFormatter: InstanceConverter[Year, Int] =
    InstanceConverter[Year, Int](_.getValue, Year.of, Some(YearFormat))
}
object YearInt {
  private[instances] val YearFormat = "ISO-8601 standard format e.g. 2007"
}
