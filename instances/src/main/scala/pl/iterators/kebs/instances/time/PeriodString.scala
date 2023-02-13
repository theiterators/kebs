package pl.iterators.kebs.instances.time

import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.time.PeriodString.PeriodFormat

import java.time.Period

trait PeriodString {
  implicit val periodFormatter: InstanceConverter[Period, String] =
    InstanceConverter[Period, String](_.toString, Period.parse, Some(PeriodFormat))
}
object PeriodString {
  private[instances] val PeriodFormat = "ISO-8601 standard format e.g. P2Y"
}
