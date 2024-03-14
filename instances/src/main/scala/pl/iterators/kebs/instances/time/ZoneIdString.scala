package pl.iterators.kebs.instances.time

import ZoneIdString.ZoneIdFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.time.ZoneId

trait ZoneIdString {
  implicit val zoneIdFormatter: InstanceConverter[ZoneId, String] =
    InstanceConverter[ZoneId, String](_.toString, ZoneId.of, Some(ZoneIdFormat))
}
object ZoneIdString {
  private[instances] val ZoneIdFormat = "IANA standard format e.g. Europe/Warsaw"
}
