package pl.iterators.kebs.instances.time

import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.instances.time.DayOfWeekInt._

import java.time.DayOfWeek

trait DayOfWeekInt {
  implicit val dayOfWeekFormatter: InstanceConverter[DayOfWeek, Int] =
    InstanceConverter[DayOfWeek, Int](_.getValue, DayOfWeek.of, Some(DayOfWeekFormat))
}
object DayOfWeekInt {
  private[instances] val DayOfWeekFormat = "ISO-8601 standard, from 1 (Monday) to 7 (Sunday)"
}
