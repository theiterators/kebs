package pl.iterators.kebs.instances.time

import MonthInt.MonthFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.Month

trait MonthInt {
  implicit val monthFormatter: InstanceConverter[Month, Int] =
    InstanceConverter[Month, Int](_.getValue, Month.of, Some(MonthFormat))
}
object MonthInt {
  private[instances] val MonthFormat = "ISO-8601 standard, from 1 (January) to 12 (December)"
}
