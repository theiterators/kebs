package pl.iterators.kebs.instances.time

import pl.iterators.kebs.core.InstanceConverter
import pl.iterators.kebs.instances.time.DurationString.DurationFormat

import java.time.Duration

trait DurationString {
  implicit val durationFormatter: InstanceConverter[Duration, String] =
    InstanceConverter[Duration, String](_.toString, Duration.parse, Some(DurationFormat))
}
object DurationString {
  private[instances] val DurationFormat = "ISO-8601 standard format e.g. PT20.345S"
}
