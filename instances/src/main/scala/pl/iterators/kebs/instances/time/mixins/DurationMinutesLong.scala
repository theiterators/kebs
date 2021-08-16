package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.instances.InstanceConverter

import java.time.Duration

trait DurationMinutesLong {
  implicit val durationMinutesFormatter: InstanceConverter[Duration, Long] =
    InstanceConverter.apply[Duration, Long](_.toMinutes, Duration.ofMinutes)
}
