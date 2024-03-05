package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.core.instances.InstanceConverter
import java.time.Instant

trait InstantEpochMilliLong {
  implicit val instantEpochMilliFormatter: InstanceConverter[Instant, Long] =
    InstanceConverter.apply[Instant, Long](_.toEpochMilli, Instant.ofEpochMilli)
}
