package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.converters.InstanceConverter

import java.time.Duration

trait DurationSecondsLong {
  implicit val durationMinutesFormatter: InstanceConverter[Duration, Long] =
    InstanceConverter.apply[Duration, Long](_.getSeconds, Duration.ofSeconds)
}
