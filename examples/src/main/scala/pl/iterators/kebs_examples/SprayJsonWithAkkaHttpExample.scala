package pl.iterators.kebs_examples

import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object SprayJsonWithAkkaHttpExample {
  trait Protocol extends DefaultJsonProtocol with SprayJsonSupport with KebsSpray {
    implicit val localTimeFormat = new JsonFormat[LocalTime] {
      override def write(obj: LocalTime): JsValue = JsString(formatter.format(obj))

      override def read(json: JsValue): LocalTime = {
        json match {
          case JsString(lTString) =>
            Try(LocalTime.parse(lTString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
          case _ => deserializationError(deserializationErrorMessage)
        }
      }

      private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
      private val deserializationErrorMessage =
        s"Expected date time in ISO offset date time format ex. ${LocalTime.now().format(formatter)}"
    }

    implicit val localDateFormat = new JsonFormat[LocalDate] {
      override def write(obj: LocalDate): JsValue = JsString(formatter.format(obj))

      override def read(json: JsValue): LocalDate = {
        json match {
          case JsString(lDString) =>
            Try(LocalDate.parse(lDString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
          case _ => deserializationError(deserializationErrorMessage)
        }
      }

      private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
      private val deserializationErrorMessage =
        s"Expected date time in ISO offset date time format ex. ${LocalDate.now().format(formatter)}"
    }
  }

  case class ResyHost(host: String)
  case class ResyClientId(clientId: String)
  case class ResySecret(secret: String)
  case class ResyConfig(host: ResyHost, clientId: ResyClientId, secret: ResySecret)

  class ResyService(config: ResyConfig, logger: LoggingAdapter)(implicit as: ActorSystem, mat: Materializer, ec: ExecutionContext)
      extends Protocol {

    def getAvailableReservations(lat: BigDecimal,
                                 lng: BigDecimal,
                                 localDate: LocalDate,
                                 seats: Int): Future[AvailableReservationsResponse] = {
      val uri     = s"${config.host.host}/1/reservation/find?num_seats=$seats&day=$localDate&long=$lng&lat=$lat"
      val request = RequestBuilding.Get(uri).addHeader(resyHeader)
      Http().singleRequest(request).flatMap { response =>
        response.status match {
          case OK => Unmarshal(response.entity).to[AvailableReservationsResponse]
          case _ =>
            response.entity.toStrict(5.seconds).flatMap { entity =>
              val body = entity.data.decodeString("UTF-8")
              logger.warning(errorMessage(request, response, body))
              Future.failed(new IOException(errorMessage(response, body)))
            }
        }
      }
    }

    def login(code: String): Future[Option[AccessToken]] = {
      val uri = s"${config.host.host}/1/oauth/token?secret=${config.secret.secret}&code=$code"
      Http()
        .singleRequest(RequestBuilding.Get(uri).addHeader(resyHeader))
        .flatMap { response =>
          response.status match {
            case OK => Unmarshal(response.entity).to[LoginResponse].map(r => Option(r.accessToken))
            case _  => Future.successful(None)
          }
        }
        .recoverWith { case t: IllegalResponseException => logger.warning("Resy code invalid"); Future.successful(None) }
    }

    def getReservationDetails(id: Int, localDate: LocalDate, seats: Int): Future[DetailsResult] = {
      val uri     = s"${config.host.host}/1/reservation?id=$id&day=$localDate&num_seats=$seats"
      val request = RequestBuilding.Get(uri).addHeader(resyHeader)
      Http().singleRequest(request).flatMap { response =>
        response.status match {
          case OK       => Unmarshal(response.entity).to[ReservationDetailsResponse].map(DetailsResult.Success)
          case NotFound => Future.successful(DetailsResult.Expired)
          case _ =>
            response.entity.toStrict(5.seconds).map { entity =>
              val body = entity.data.decodeString("UTF-8")
              logger.warning(errorMessage(request, response, body))
              DetailsResult.Error(errorMessage(response, body))
            }
        }
      }
    }

    def bookReservation(accessToken: String, resyToken: String): Future[ReservationResult] = {
      val uri     = s"${config.host.host}/1/reservation"
      val data    = FormData("access_token" -> accessToken, "resy_token" -> resyToken)
      val request = RequestBuilding.Post(uri, data).addHeader(resyHeader)
      Http()
        .singleRequest(request)
        .flatMap { response =>
          response.status match {
            case status if status.isSuccess() => Unmarshal(response.entity).to[ReservationBookedResponse].map(ReservationResult.Success)
            case NotFound                     => Future.successful(ReservationResult.Expired)
            case PreconditionFailed           => Future.successful(ReservationResult.Conflict)
            case _ =>
              response.entity.toStrict(5.seconds).map { entity =>
                val body = entity.data.decodeString("UTF-8")
                logger.warning(errorMessage(request, response, body))
                ReservationResult.Error(errorMessage(response, body))
              }
          }
        }
        .recoverWith {
          case t: IllegalResponseException =>
            logger.warning("Resy token expired")
            Future.successful(ReservationResult.Error("Resy token expired"))
        }
    }

    def getUserReservations(accessToken: String): Future[Either[String, Reservations]] = {
      val uri     = s"${config.host.host}/1/user/reservations?access_token=$accessToken"
      val request = RequestBuilding.Get(uri).addHeader(resyHeader)
      Http()
        .singleRequest(request)
        .flatMap { response =>
          response.status match {
            case status if status.isSuccess() => Unmarshal(response.entity).to[Reservations].map(Right(_))
            case _ =>
              response.entity.toStrict(5.seconds).map { entity =>
                val body = entity.data.decodeString("UTF-8")
                logger.warning(errorMessage(request, response, body))
                Left(errorMessage(response, body))
              }
          }
        }
        .recoverWith {
          case t: IllegalResponseException => logger.warning("Resy token expired"); Future.successful(Left("Resy token expired"))
        }
    }

    def deleteReservation(accessToken: String, resyToken: String): Future[Either[String, CancellationResult]] = {
      val uri     = s"${config.host.host}/1/reservation"
      val query   = Query("access_token" -> accessToken, "resy_token" -> resyToken)
      val request = RequestBuilding.Delete(Uri(uri).withQuery(query)).addHeader(resyHeader)
      Http()
        .singleRequest(request)
        .flatMap { response =>
          response.status match {
            case status if status.isSuccess() => Future.successful(Right(CancellationResult.Cancelled))
            case PreconditionFailed           => Future.successful(Right(CancellationResult.CancellationForbidden))
            case _ =>
              response.entity.toStrict(5.seconds).map { entity =>
                val body = entity.data.decodeString("UTF-8")
                logger.warning(errorMessage(request, response, body))
                Left(errorMessage(response, body))
              }
          }
        }
        .recoverWith {
          case t: IllegalResponseException => logger.warning("Resy token expired"); Future.successful(Left("Resy token expired"))
        }
    }

    private def errorMessage(response: HttpResponse, body: String) = {
      s"Resy request failed because ${response.status} and entity $body"
    }

    private def errorMessage(request: HttpRequest, response: HttpResponse, body: String) = {
      s"Resy request $request failed because ${response.status} and entity $body"
    }

    private val resyHeader = RawHeader("Authorization", s"""PlatformAPI client_id="${config.clientId.clientId}"""")
  }

  case class Contact(phoneNumber: String, url: String)
  case class LocationShort(city: String, latitude: BigDecimal, longitude: BigDecimal, neighborhood: String, timeZone: String)
  case class LocationFull(city: String,
                          latitude: BigDecimal,
                          longitude: BigDecimal,
                          neighborhood: String,
                          timeZone: String,
                          address_1: String,
                          state: String,
                          postalCode: String)
  case class Reservation(deepLink: String, id: Int, seatType: String, timeSlot: String, webLink: String)
  case class TravelTime(distance: Double, driving: Int, walking: Int)
  case class AvailableReservation(contact: Contact,
                                  deepLink: String,
                                  location: LocationShort,
                                  images: Option[List[String]],
                                  name: String,
                                  priceRangeId: Int,
                                  reservations: List[Reservation],
                                  travelTime: TravelTime,
                                  `type`: String,
                                  webLink: String)
  object AvailableReservation {
    val PriceRangeMax = 4
  }
  case class AvailableReservationsResponse(available: List[AvailableReservation])

  case class AccessToken(accessToken: String) extends AnyVal
  case class LoginResponse(accessToken: AccessToken)

  case class Rater(name: String, score: Double, scale: Double, image: String)
  case class Venue(location: LocationFull,
                   name: String,
                   priceRangeId: Int,
                   `type`: String,
                   images: List[String],
                   about: String,
                   tagline: String,
                   rater: Rater)
  case class BookedReservation(deepLink: String, seatType: String, timeSlot: String)
  case class PaymentDetails(resyFee: Option[BigDecimal],
                            serviceCharge: Option[BigDecimal],
                            tax: Option[BigDecimal],
                            total: Option[BigDecimal])
  case class Payment(details: PaymentDetails)
  case class ReservationDetailsResponse(venue: Venue, reservation: BookedReservation, payment: Option[Payment], resyToken: String)

  case class ReservationBookedResponse(resyToken: String)

  case class Reservations(reservations: List[RequestedReservation])
  case class RequestedReservationDetails(day: LocalDate, timeSlot: LocalTime)
  case class Fee(amount: BigDecimal)
  case class Cancellation(fee: Option[Fee])
  case class RequestedReservation(resyToken: String, reservation: RequestedReservationDetails, cancellation: Option[Cancellation])

  sealed trait DetailsResult
  object DetailsResult {
    case class Success(details: ReservationDetailsResponse) extends DetailsResult
    case object Expired                                     extends DetailsResult
    case class Error(message: String)                       extends DetailsResult
  }

  sealed trait ReservationResult
  object ReservationResult {
    case class Success(booking: ReservationBookedResponse) extends ReservationResult
    case object Expired                                    extends ReservationResult
    case object Conflict                                   extends ReservationResult
    case class Error(message: String)                      extends ReservationResult
  }

  sealed trait CancellationResult
  object CancellationResult {
    case object Cancelled             extends CancellationResult
    case object CancellationForbidden extends CancellationResult
  }

}
