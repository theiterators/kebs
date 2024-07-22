package pl.iterators.kebs.instances.time

import PeriodString.PeriodFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.Period

trait PeriodString {
  implicit val periodFormatter: InstanceConverter[Period, String] =
    InstanceConverter[Period, String](_.toString, Period.parse, Some(PeriodFormat))
}
object PeriodString {
  private[instances] val PeriodFormat = "ISO-8601 standard format e.g. P2Y"
}
