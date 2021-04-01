package pl.iterators.kebs_examples

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import pl.iterators.kebs.instances.NetInstances.URIString
import pl.iterators.kebs.instances.UtilInstances.UUIDString
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.net.URI
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object SprayJsonFormat {

  trait ThingsService {
    def create(request: ThingCreateRequest): Future[ThingCreateResponse]
  }

  object BeforeKebs {
    trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
      implicit val urlJsonFormat = new JsonFormat[URI] {
        override def read(json: JsValue): URI = json match {
          case JsString(uri) => Try(new URI(uri)).getOrElse(deserializationError("Invalid URI format"))
          case _             => deserializationError("URL should be string")
        }

        override def write(obj: URI): JsValue = JsString(obj.toString)
      }

      implicit val uuidFormat = new JsonFormat[UUID] {
        override def write(obj: UUID): JsValue = JsString(obj.toString)

        override def read(json: JsValue): UUID = json match {
          case JsString(uuid) => Try(UUID.fromString(uuid)).getOrElse(deserializationError("Expected UUID format"))
          case _              => deserializationError("Expected UUID format")
        }
      }
    }
    object ThingProtocol extends JsonProtocol {
      def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
        new JsonFormat[T] {
          override def read(json: JsValue): T = construct(jr.read(json))
          override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
        }

      implicit val errorJsonFormat              = jsonFormat1(Error.apply)
      implicit val thingIdJsonFormat            = jsonFlatFormat(ThingId.apply)
      implicit val tagIdJsonFormat              = jsonFlatFormat(TagId.apply)
      implicit val thingNameJsonFormat          = jsonFlatFormat(ThingName.apply)
      implicit val thingDescriptionJsonFormat   = jsonFlatFormat(ThingDescription.apply)
      implicit val locationJsonFormat           = jsonFormat2(Location.apply)
      implicit val createThingRequestJsonFormat = jsonFormat5(ThingCreateRequest.apply)
      implicit val thingJsonFormat              = jsonFormat6(Thing.apply)
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
    }
  }

  object AfterKebs {
    object ThingProtocol extends DefaultJsonProtocol with SprayJsonSupport with KebsSpray with URIString with UUIDString

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
    }
  }

  case class ThingId(uuid: UUID)
  case class ThingName(name: String)
  case class ThingDescription(description: String)
  case class TagId(id: String)
  case class Location(latitude: Double, longitude: Double)

  case class Thing(id: ThingId, name: ThingName, description: ThingDescription, pictureUri: URI, tags: List[TagId], location: Location)

  case class ThingCreateRequest(name: ThingName,
                                description: ThingDescription,
                                pictureUrl: Option[URI],
                                tags: List[TagId],
                                location: Location)
  sealed abstract class ThingCreateResponse
  object ThingCreateResponse {
    case class Created(thing: Thing) extends ThingCreateResponse
    case object AlreadyExists        extends ThingCreateResponse
  }

  case class Error(message: String)
}
