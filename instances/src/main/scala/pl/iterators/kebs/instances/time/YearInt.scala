package pl.iterators.kebs.instances.time

import pl.iterators.kebs.core.InstanceConverter
import pl.iterators.kebs.instances.time.YearInt.YearFormat

import java.time.Year

trait YearInt {
  implicit val yearFormatter: InstanceConverter[Year, Int] =
    InstanceConverter[Year, Int](_.getValue, Year.of, Some(YearFormat))
}
object YearInt {
  private[instances] val YearFormat = "ISO-8601 standard format e.g. 2007"
}
