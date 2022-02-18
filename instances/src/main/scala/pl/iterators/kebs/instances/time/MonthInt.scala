package pl.iterators.kebs.instances.time

import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.time.MonthInt.MonthFormat

import java.time.Month

trait MonthInt {
  implicit val monthFormatter: InstanceConverter[Month, Int] =
    InstanceConverter[Month, Int](_.getValue, Month.of, Some(MonthFormat))
}
object MonthInt {
  private[instances] val MonthFormat = "ISO-8601 standard, from 1 (January) to 12 (December)"
}
