package pl.iterators.kebs.examples

import java.net.URL
import java.util.UUID

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.server.Directives._
import enumeratum.{Enum, EnumEntry}
import pl.iterators.kebs.playjson.{KebsEnumFormats, KebsSpray}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, JsonReader, JsonWriter, deserializationError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object EnumSprayJsonFormat {

  trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val urlJsonFormat = new JsonFormat[URL] {
      override def read(json: JsValue): URL = json match {
        case JsString(url) => Try(new URL(url)).getOrElse(deserializationError("Invalid URL format"))
        case _             => deserializationError("URL should be string")
      }

      override def write(obj: URL): JsValue = JsString(obj.toString)
    }

    implicit val uuidFormat = new JsonFormat[UUID] {
      override def write(obj: UUID): JsValue = JsString(obj.toString)

      override def read(json: JsValue): UUID = json match {
        case JsString(uuid) => Try(UUID.fromString(uuid)).getOrElse(deserializationError("Expected UUID format"))
        case _              => deserializationError("Expected UUID format")
      }
    }
  }

  trait ThingsService {
    def create(request: ThingCreateRequest): Future[ThingCreateResponse]
    def update(request: ThingUpdateRequest): Future[ThingUpdateResponse]
  }

  object BeforeKebs {
    object ThingProtocol extends JsonProtocol {
      def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
        new JsonFormat[T] {
          override def read(json: JsValue): T = construct(jr.read(json))
          override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
        }

      def jsonEnumFormat[T <: EnumEntry](enumCompanion: Enum[T]): JsonFormat[T] = new JsonFormat[T] {
        override def read(json: JsValue): T = json match {
          case JsString(name) =>
            enumCompanion
              .withNameInsensitiveOption(name)
              .getOrElse(deserializationError(s"$name should be one of (${enumCompanion.values.map(_.entryName).mkString(", ")})"))
          case _ =>
            deserializationError(
              s"${json.toString()} should be a string of value (${enumCompanion.values.map(_.entryName).mkString(", ")})")
        }

        override def write(obj: T): JsValue = JsString(obj.entryName)
      }

      implicit val errorJsonFormat              = jsonFormat1(Error.apply)
      implicit val thingIdJsonFormat            = jsonFlatFormat(ThingId.apply)
      implicit val tagIdJsonFormat              = jsonFlatFormat(TagId.apply)
      implicit val thingNameJsonFormat          = jsonFlatFormat(ThingName.apply)
      implicit val thingDescriptionJsonFormat   = jsonFlatFormat(ThingDescription.apply)
      implicit val locationJsonFormat           = jsonFormat2(Location.apply)
      implicit val thingStatusJsonFormat        = jsonEnumFormat(ThingStatus)
      implicit val createThingRequestJsonFormat = jsonFormat5(ThingCreateRequest.apply)
      implicit val updateThingRequestJsonFormat = jsonFormat5(ThingUpdateRequest.apply)
      implicit val thingJsonFormat              = jsonFormat7(Thing.apply)
    }

    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {
      import ThingProtocol._
      def createRoute = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> Error("Already exists")
          }
        }
      }
      def updateRoute = (patch & entity(as[ThingUpdateRequest])) { request =>
        complete {
          thingsService.update(request).map[ToResponseMarshallable] {
            case ThingUpdateResponse.Updated(thing) => OK        -> thing
            case ThingUpdateResponse.Forbidden      => Forbidden -> Error("Not allowed to update thing")
            case ThingUpdateResponse.NotFound       => NotFound  -> Error("Thing not found")
          }
        }
      }
    }
  }

  object AfterKebs {
    object ThingProtocol extends JsonProtocol with KebsSpray with KebsEnumFormats

    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {
      import ThingProtocol._
      def createRoute = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> Error("Already exists")
          }
        }
      }
      def updateRoute = (patch & entity(as[ThingUpdateRequest])) { request =>
        complete {
          thingsService.update(request).map[ToResponseMarshallable] {
            case ThingUpdateResponse.Updated(thing) => OK        -> thing
            case ThingUpdateResponse.Forbidden      => Forbidden -> Error("Not allowed to update thing")
            case ThingUpdateResponse.NotFound       => NotFound  -> Error("Thing not found")
          }
        }
      }
    }
  }

  case class ThingId(uuid: UUID)
  case class ThingName(name: String)
  case class ThingDescription(description: String)
  case class TagId(id: String)
  case class Location(latitude: Double, longitude: Double)

  sealed trait ThingStatus extends EnumEntry
  object ThingStatus extends Enum[ThingStatus] {
    case object Active     extends ThingStatus
    case object Unapproved extends ThingStatus
    case object Blocked    extends ThingStatus

    override val values = findValues
  }

  case class Thing(id: ThingId,
                   name: ThingName,
                   description: ThingDescription,
                   pictureUrl: URL,
                   tags: List[TagId],
                   location: Location,
                   status: ThingStatus)

  case class ThingCreateRequest(name: ThingName,
                                description: ThingDescription,
                                pictureUrl: Option[URL],
                                tags: List[TagId],
                                location: Location)
  sealed abstract class ThingCreateResponse
  object ThingCreateResponse {
    case class Created(thing: Thing) extends ThingCreateResponse
    case object AlreadyExists        extends ThingCreateResponse
  }

  case class ThingUpdateRequest(name: Option[ThingName],
                                description: Option[ThingDescription],
                                tags: Option[List[TagId]],
                                location: Option[Location],
                                status: Option[ThingStatus])

  sealed trait ThingUpdateResponse
  object ThingUpdateResponse {
    case class Updated(thing: Thing) extends ThingUpdateResponse
    case object Forbidden            extends ThingUpdateResponse
    case object NotFound             extends ThingUpdateResponse
  }

  case class Error(message: String)

}
