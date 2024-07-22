package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.core.instances.InstanceConverter
import java.time.Period

trait PeriodMonthsInt {
  implicit val periodMonthsFormatter: InstanceConverter[Period, Int] =
    InstanceConverter.apply[Period, Int](_.getMonths, Period.ofMonths)
}
