package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.core.InstanceConverter
import java.time.Period

trait PeriodDays {
  implicit val periodDaysFormatter: InstanceConverter[Period, Int] =
    InstanceConverter.apply[Period, Int](_.getDays, Period.ofDays)
}
