package pl.iterators.kebs_benchmarks

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}
import java.util.concurrent.TimeUnit

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.openjdk.jmh.annotations._
import org.scalatest.{FunSpec, Matchers}
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

case class AvailableReservationsResponse(available: List[AvailableReservation])

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
case class PaymentDetails(fee: Option[BigDecimal], serviceCharge: Option[BigDecimal], tax: Option[BigDecimal], total: Option[BigDecimal])
case class Payment(details: Option[PaymentDetails])

case class ReservationDetailsResponse(venue: Venue, reservation: BookedReservation, payment: Option[Payment], token: String)

case class Reservations(reservations: List[RequestedReservation])
case class RequestedReservationDetails(day: LocalDate, timeSlot: LocalTime)
case class Fee(amount: BigDecimal)
case class Cancellation(fee: Option[Fee])
case class RequestedReservation(token: String, reservation: RequestedReservationDetails, cancellation: Option[Cancellation])

sealed trait DetailsResult
object DetailsResult {
  case class Success(details: ReservationDetailsResponse) extends DetailsResult
  case object Expired                                     extends DetailsResult
  case class Error(message: String)                       extends DetailsResult
}

trait Protocol extends DefaultJsonProtocol with SprayJsonSupport {
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

abstract class Service {
  def getAvailableReservations: Future[AvailableReservationsResponse]
  def getReservationDetails(id: Int): Future[DetailsResult]
  def getUserReservations(accessToken: String): Future[Reservations]
}

object BeforeKebs {
  object Protocol extends Protocol {
    def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
      new JsonFormat[T] {
        override def read(json: JsValue): T = construct(jr.read(json))
        override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
      }

    implicit val contactFormat                      = jsonFormat2(Contact.apply)
    implicit val locationShortFormat                = jsonFormat5(LocationShort.apply)
    implicit val locationFullFormat                 = jsonFormat8(LocationFull.apply)
    implicit val reservationFormat                  = jsonFormat5(Reservation.apply)
    implicit val travelTimeFormat                   = jsonFormat3(TravelTime.apply)
    implicit val availableReservationFormat         = jsonFormat10(AvailableReservation.apply)
    implicit val availableReservationResponseFormat = jsonFormat1(AvailableReservationsResponse.apply)
    implicit val raterFormat                        = jsonFormat4(Rater.apply)
    implicit val venueResponseFormat                = jsonFormat8(Venue.apply)
    implicit val bookedReservationResponseFormat    = jsonFormat3(BookedReservation.apply)
    implicit val paymentDetailsFormat               = jsonFormat4(PaymentDetails.apply)
    implicit val paymentFormat                      = jsonFormat1(Payment.apply)
    implicit val reservationDetailsResponseFormat   = jsonFormat4(ReservationDetailsResponse.apply)
    implicit val feeFormat                          = jsonFormat1(Fee.apply)
    private implicit val cancellationFormat         = jsonFormat1(Cancellation.apply)
    implicit val requestedReservationDetailsFormat  = jsonFormat2(RequestedReservationDetails.apply)
    implicit val requestedReservationFormat         = jsonFormat3(RequestedReservation.apply)
    implicit val reservationsFormat                 = jsonFormat1(Reservations.apply)
  }

  class Router(service: Service)(implicit ec: ExecutionContext) {
    import Protocol._
    val getAvailableReservations = (get & pathEndOrSingleSlash) {
      complete(service.getAvailableReservations)
    }
    val getReservationDetails = (get & path(IntNumber)) { id =>
      complete {
        service.getReservationDetails(id).map[ToResponseMarshallable] {
          case DetailsResult.Success(res) => OK -> res
          case DetailsResult.Expired      => NotFound
          case DetailsResult.Error(error) => BadRequest -> error
        }
      }
    }
    val getUserReservations = (get & parameters('token)) { token =>
      complete(service.getUserReservations(token))
    }
  }
}

object AfterKebs {
  object Protocol extends Protocol with KebsSpray

  class Router(service: Service)(implicit ec: ExecutionContext) {
    import Protocol._
    val getAvailableReservations = (get & pathEndOrSingleSlash) {
      complete(service.getAvailableReservations)
    }
    val getReservationDetails = (get & path(IntNumber)) { id =>
      complete {
        service.getReservationDetails(id).map[ToResponseMarshallable] {
          case DetailsResult.Success(res) => OK -> res
          case DetailsResult.Expired      => NotFound
          case DetailsResult.Error(error) => BadRequest -> error
        }
      }
    }
    val getUserReservations = (get & parameters('token)) { token =>
      complete(service.getUserReservations(token))
    }
  }
}

object SprayJsonFormatBenchmark {
  val fakeService = new Service {
    val sampleAvailableReservationsResponse = AvailableReservationsResponse(
      List(
        AvailableReservation(
          Contact("12 270 24 88", "<none>"),
          "?",
          LocationShort("Czernichów", 49.9915924, 19.6754663, "Czernichów", "CET"),
          None,
          "RAPIO",
          1,
          List(
            Reservation("?", 1, "stolik", "20:00-21:00", "<none>"),
            Reservation("?", 2, "stolik", "20:00-21:00", "<none>"),
            Reservation("?", 3, "stolik", "20:00-21:00", "<none>")
          ),
          TravelTime(distance = 100, driving = 99, walking = 1),
          "pizzera",
          "<none>"
        )))
    val sampleReservetionDetailsResponse = ReservationDetailsResponse(
      Venue(
        LocationFull("Czernichów", 49.9915924, 19.6754663, "Czernichów", "CET", "Czernichów 232", "małopolskie", "32-071"),
        "RAPIO",
        1,
        "pizzeria",
        List.empty,
        "Pizzeria & Restauracja RAPIO",
        "Pizzeria & Restauracja RAPIO",
        Rater("?", 100.0, 1.0, "?")
      ),
      BookedReservation("?", "stolik", "20.00-21:00"),
      Some(Payment(Some(PaymentDetails(fee = None, serviceCharge = Some(100), tax = Some(0.08), total = Some(108))))),
      token = "abcdefgh"
    )
    val sampleReservations = Reservations(List(
      RequestedReservation("abcdefgh", RequestedReservationDetails(LocalDate.now(), LocalTime.now()), Some(Cancellation(Some(Fee(10)))))))

    override def getReservationDetails(id: Int)           = Future.successful(DetailsResult.Success(sampleReservetionDetailsResponse))
    override def getUserReservations(accessToken: String) = Future.successful(sampleReservations)
    override def getAvailableReservations                 = Future.successful(sampleAvailableReservationsResponse)
  }

  import ExecutionContext.Implicits.global
  val beforeKebsRouter = new BeforeKebs.Router(fakeService)
  val afterKebsRouter  = new AfterKebs.Router(fakeService)

  val beforeKebsRoutes = beforeKebsRouter.getUserReservations ~ beforeKebsRouter.getAvailableReservations ~ beforeKebsRouter.getReservationDetails
  val afterKebsRoutes  = afterKebsRouter.getUserReservations ~ afterKebsRouter.getAvailableReservations ~ afterKebsRouter.getReservationDetails
}

@State(Scope.Benchmark)
@Warmup(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
class SprayJsonFormatBenchmark extends FunSpec with Matchers with ScalatestRouteTest {

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sprayJsonCostBeforeKebs1 = Get("/?token=token") ~> SprayJsonFormatBenchmark.beforeKebsRoutes ~> check {
    status shouldEqual OK
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sprayJsonCostBeforeKebs2 = Get("/") ~> SprayJsonFormatBenchmark.beforeKebsRoutes ~> check {
    status shouldEqual OK
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sprayJsonCostBeforeKebs3 = Get("/1") ~> SprayJsonFormatBenchmark.beforeKebsRoutes ~> check {
    status shouldEqual OK
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sprayJsonCostAfterKebs1 = Get("/?token=token") ~> SprayJsonFormatBenchmark.afterKebsRoutes ~> check {
    status shouldEqual OK
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sprayJsonCostAfterKebs2 = Get("/") ~> SprayJsonFormatBenchmark.afterKebsRoutes ~> check {
    status shouldEqual OK
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sprayJsonCostAfterKebs3 = Get("/1") ~> SprayJsonFormatBenchmark.afterKebsRoutes ~> check {
    status shouldEqual OK
  }

}
