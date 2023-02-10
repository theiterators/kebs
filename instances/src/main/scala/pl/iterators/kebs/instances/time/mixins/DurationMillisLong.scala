package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.core.InstanceConverter
import java.time.Duration

trait DurationMillisLong {
  implicit val durationMillisFormatter: InstanceConverter[Duration, Long] =
    InstanceConverter.apply[Duration, Long](_.toMillis, Duration.ofMillis)
}
