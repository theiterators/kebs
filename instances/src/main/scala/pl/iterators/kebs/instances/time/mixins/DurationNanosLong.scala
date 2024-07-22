package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.core.instances.InstanceConverter
import java.time.Duration

trait DurationNanosLong {
  implicit val durationNanosFormatter: InstanceConverter[Duration, Long] =
    InstanceConverter.apply[Duration, Long](_.toNanos, Duration.ofNanos)
}
