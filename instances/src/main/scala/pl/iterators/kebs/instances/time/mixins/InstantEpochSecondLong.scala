package pl.iterators.kebs.instances.time.mixins

import pl.iterators.kebs.core.InstanceConverter
import java.time.Instant

trait InstantEpochSecondLong {
  implicit val instantEpochSecondFormatter: InstanceConverter[Instant, Long] =
    InstanceConverter.apply[Instant, Long](_.getEpochSecond, Instant.ofEpochSecond)
}
