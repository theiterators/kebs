package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.instances.InstanceConverter

import java.time.Period

trait PeriodYearsInt {
  implicit val periodYearsFormatter: InstanceConverter[Period, Int] =
    InstanceConverter.apply[Period, Int](_.getYears, Period.ofYears)
}
