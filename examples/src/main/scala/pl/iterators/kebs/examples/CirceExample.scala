package pl.iterators.kebs.examples

import java.net.URL
import java.util.UUID

import org.apache.pekko.http.scaladsl.marshalling.{ToResponseMarshallable, _}
import org.apache.pekko.http.scaladsl.model.MediaTypes.`application/json`
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.model.{ContentType, ContentTypeRange, HttpEntity, MediaType}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.unmarshalling._
import org.apache.pekko.util.ByteString
import cats.data.NonEmptyList
import io.circe.jawn.parseByteBuffer
import io.circe._
import pl.iterators.kebs.circe.KebsCirce

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object CirceExample {

  trait ThingsService {
    def create(request: ThingCreateRequest): Future[ThingCreateResponse]
  }

  object BeforeKebs {
    object ThingProtocol extends CirceProtocol with CircePekkoHttpSupport {
      import io.circe._
      import io.circe.generic.semiauto._
      implicit val thingCreateRequestEncoder: Encoder[ThingCreateRequest] = deriveEncoder
      implicit val thingCreateRequestDecoder: Decoder[ThingCreateRequest] = deriveDecoder
      implicit val thingIdEncoder: Encoder[ThingId]                       = deriveEncoder
      implicit val thingIdDecoder: Decoder[ThingId]                       = deriveDecoder
      implicit val thingNameEncoder: Encoder[ThingName]                   = deriveEncoder
      implicit val thingNameDecoder: Decoder[ThingName]                   = deriveDecoder
      implicit val thingDescriptionEncoder: Encoder[ThingDescription]     = deriveEncoder
      implicit val thingDescriptionDecoder: Decoder[ThingDescription]     = deriveDecoder
      implicit val tagIdEncoder: Encoder[TagId]                           = deriveEncoder
      implicit val tagIdDecoder: Decoder[TagId]                           = deriveDecoder
      implicit val locationEncoder: Encoder[Location]                     = deriveEncoder
      implicit val locationDecoder: Decoder[Location]                     = deriveDecoder
      implicit val thingEncoder: Encoder[Thing]                           = deriveEncoder
      implicit val thingDecoder: Decoder[Thing]                           = deriveDecoder
      implicit val errorMessageDecoder: Decoder[ErrorMessage]             = deriveDecoder
      implicit val errorMessageEncoder: Encoder[ErrorMessage]             = deriveEncoder
    }
    import ThingProtocol._
    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {

      def createRoute: Route = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> ErrorMessage("Already exists")
          }
        }
      }
    }
  }

  object AfterKebs {
    object ThingProtocol extends KebsCirce with CirceProtocol with CircePekkoHttpSupport
    import ThingProtocol._

    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {

      def createRoute: Route = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> ErrorMessage("Already exists")
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

  case class Thing(id: ThingId, name: ThingName, description: ThingDescription, pictureUrl: URL, tags: List[TagId], location: Location)

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

  case class ErrorMessage(message: String)

  trait CirceProtocol {
    implicit val encodeUrl: Encoder[URL]  = (url: URL) => Json.fromString(url.toString)
    implicit val urlDecoder: Decoder[URL] = Decoder.decodeString.map(new URL(_))
  }
}

trait CircePekkoHttpSupport {

  implicit def jsonUnmarshaller[T](implicit reader: Decoder[T]): FromEntityUnmarshaller[T] =
    jsonUnmarshaller
      .map(json => reader.decodeAccumulating(json.hcursor))
      .map(
        _.fold(failures => throw DecodingFailures(failures), identity)
      )

  implicit def jsonMarshallerConverter[T](implicit writer: Encoder[T], printer: Printer = Printer.noSpaces): ToEntityMarshaller[T] =
    jsonMarshaller(printer).compose(writer.apply)

  implicit private final val jsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty => throw Unmarshaller.NoContentException
        case data             => parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
      }

  implicit def jsonMarshaller(implicit
      printer: Printer = Printer.noSpaces
  ): ToEntityMarshaller[Json] =
    Marshaller.oneOf(mediaTypes: _*) { mediaType =>
      Marshaller.withFixedContentType(ContentType(mediaType)) { json =>
        HttpEntity(
          mediaType,
          ByteString(printer.printToByteBuffer(json, mediaType.charset.nioCharset()))
        )
      }
    }

  private def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  private def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/json`)
}

final case class DecodingFailures(failures: NonEmptyList[DecodingFailure]) extends Exception {
  override def getMessage = failures.toList.map(_.message).mkString("\n")
}
