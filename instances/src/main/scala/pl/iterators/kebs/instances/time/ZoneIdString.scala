package pl.iterators.kebs.instances.time

import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.instances.time.ZoneIdString.ZoneIdFormat

import java.time.ZoneId

trait ZoneIdString {
  implicit val zoneIdFormatter: InstanceConverter[ZoneId, String] =
    InstanceConverter[ZoneId, String](_.toString, ZoneId.of, Some(ZoneIdFormat))
}
object ZoneIdString {
  private[instances] val ZoneIdFormat = "IANA standard format e.g. Europe/Warsaw"
}
