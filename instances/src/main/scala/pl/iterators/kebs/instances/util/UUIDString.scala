package pl.iterators.kebs.instances.util

import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.util.UUIDString.UUIDFormat

import java.util.UUID

trait UUIDString {
  implicit val uuidFormatter: InstanceConverter[UUID, String] =
    InstanceConverter[UUID, String](_.toString, UUID.fromString, Some(UUIDFormat))
}
object UUIDString {
  private[instances] val UUIDFormat = "128-bit number e.g. 123e4567-e89b-12d3-a456-426614174000"
}
