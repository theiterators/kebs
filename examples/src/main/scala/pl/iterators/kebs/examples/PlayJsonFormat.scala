package pl.iterators.kebs.examples

import java.net.URL
import java.util.UUID

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import pl.iterators.kebs.json.KebsPlay
import play.api.libs.json._

import scala.util.Try

object PlayJsonFormat {
  trait JsonProtocol {
    implicit val urlJsonFormat = new Format[URL] {
      override def reads(json: JsValue): JsResult[URL] = json match {
        case JsString(url) => Try(new URL(url)).map(JsSuccess(_)).getOrElse(JsError("error.malformed.url"))
        case _             => JsError("error.expected.jsstring")
      }

      override def writes(o: URL): JsValue = JsString(o.toString)
    }

  }

  object BeforeKebs extends JsonProtocol {
    def flatFormat[P, T <: Product](construct: P => T)(implicit jf: Format[P]): Format[T] =
      Format[T](jf.map(construct), Writes(a => jf.writes(a.productElement(0).asInstanceOf[P])))

    implicit val thingIdJsonFormat          = flatFormat(ThingId.apply)
    implicit val tagIdJsonFormat            = flatFormat(TagId.apply)
    implicit val thingNameJsonFormat        = flatFormat(ThingName.apply)
    implicit val thingDescriptionJsonFormat = flatFormat(ThingDescription.apply)

    implicit val errorJsonFormat              = Json.format[Error]
    implicit val locationJsonFormat           = Json.format[Location]
    implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
    implicit val thingJsonFormat              = Json.format[Thing]
  }

  object AfterKebs extends JsonProtocol with KebsPlay {
    implicit val errorJsonFormat              = Json.format[Error]
    implicit val locationJsonFormat           = Json.format[Location]
    implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
    implicit val thingJsonFormat              = Json.format[Thing]
  }

  case class ThingId(uuid: UUID)
  case class ThingName(name: String)
  case class ThingDescription(description: String)
  case class TagId(id: String)
  case class Location(latitude: Double, longitude: Double)

  sealed trait ThingStatus extends EnumEntry
  object ThingStatus extends Enum[ThingStatus] with PlayJsonEnum[ThingStatus] {
    case object Active     extends ThingStatus
    case object Unapproved extends ThingStatus
    case object Blocked    extends ThingStatus

    override val values = findValues
  }

  case class Thing(
      id: ThingId,
      name: ThingName,
      description: ThingDescription,
      pictureUrl: URL,
      tags: List[TagId],
      location: Location,
      status: ThingStatus
  )

  case class ThingCreateRequest(
      name: ThingName,
      description: ThingDescription,
      pictureUrl: Option[URL],
      tags: List[TagId],
      location: Location
  )
  sealed abstract class ThingCreateResponse
  object ThingCreateResponse {
    case class Created(thing: Thing) extends ThingCreateResponse
    case object AlreadyExists        extends ThingCreateResponse
  }

  case class ThingUpdateRequest(
      name: Option[ThingName],
      description: Option[ThingDescription],
      tags: Option[List[TagId]],
      location: Option[Location],
      status: Option[ThingStatus]
  )

  sealed trait ThingUpdateResponse
  object ThingUpdateResponse {
    case class Updated(thing: Thing) extends ThingUpdateResponse
    case object Forbidden            extends ThingUpdateResponse
    case object NotFound             extends ThingUpdateResponse
  }

  case class Error(message: String)
}
