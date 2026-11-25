package pl.iterators.kebs.jsoniter.pekkohttp

import com.github.pjfanning.pekkohttpjsoniterscala.JsoniterScalaSupport
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import org.apache.pekko.util.ByteString
import org.scalatest.BeforeAndAfterAll
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.KebsJsoniterEnums
import pl.iterators.kebs.jsoniter.formats.JsoniterTestBase
import pl.iterators.kebs.testkit.model.Color

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object JsoniterPekkoHttpTestDomain {
  object Protocol extends JsoniterScalaSupport with KebsJsoniter with CaseClass1ToValueClass with KebsEnumeratum with KebsJsoniterEnums

  case class Thing(a: Int, b: String)
  case class Id(value: String)
  sealed trait Shape
  case class Circle(r: Double, label: String) extends Shape
  case object Empty                           extends Shape
}

class JsoniterPekkoHttpTests extends JsoniterTestBase with BeforeAndAfterAll {
  import JsoniterPekkoHttpTestDomain._
  import Protocol._

  implicit private val system: ActorSystem = ActorSystem("JsoniterPekkoHttpTests")
  import system.dispatcher

  override protected def afterAll(): Unit = Await.result(system.terminate(), 10.seconds): Unit

  private def response(response: ToResponseMarshallable): HttpResponse =
    Await.result(response(HttpRequest()).flatMap(r => r.entity.toStrict(5.seconds).map(r.withEntity(_))), 10.seconds)

  private def sent(marshallable: ToResponseMarshallable): (ContentType, String) = {
    val entity = response(marshallable).entity.asInstanceOf[HttpEntity.Strict]
    (entity.contentType, entity.data.utf8String)
  }

  private def received[A: FromEntityUnmarshaller](json: String): A =
    Await.result(Unmarshal(HttpEntity(ContentTypes.`application/json`, json)).to[A], 10.seconds)

  private def asJson(json: String) = (ContentTypes.`application/json`: ContentType, json)
  private def asText(text: String) = (ContentTypes.`text/plain(UTF-8)`: ContentType, text)
  private val thing                = Thing(1, "x")
  private val thingJson            = """{"a":1,"b":"x"}"""

  test("case classes, sealed hierarchies, value classes and enums are sent as JSON") {
    sent(thing) shouldBe asJson(thingJson)
    sent(StatusCodes.Created -> thing) shouldBe asJson(thingJson)
    sent((StatusCodes.Created, List.empty[HttpHeader], thing)) shouldBe asJson(thingJson)
    sent(Future.successful(thing)) shouldBe asJson(thingJson)
    sent(Circle(1.5, "c"): Shape) shouldBe asJson("""{"Circle":{"r":1.5,"label":"c"}}""")
    sent(Id("x")) shouldBe asJson("\"x\"")
    sent(Color.DarkBlue: Color) shouldBe asJson("\"DarkBlue\"")
  }

  test("collections are sent as JSON") {
    sent(List(thing)) shouldBe asJson(s"[$thingJson]")
    sent(StatusCodes.OK -> List(thing)) shouldBe asJson(s"[$thingJson]")
    sent(Future.successful(Seq(thing))) shouldBe asJson(s"[$thingJson]")
    sent(Vector(Id("x"))) shouldBe asJson("[\"x\"]")
    sent(Set(1)) shouldBe asJson("[1]")
    sent(Map("k" -> thing)) shouldBe asJson(s"""{"k":$thingJson}""")
    val array: Array[Thing] = Array(thing)
    sent(array) shouldBe asJson(s"[$thingJson]")
  }

  test("a missing option is not found, as pekko answers it") {
    sent(Option(thing)) shouldBe asJson(thingJson)
    response(Option.empty[Thing]).entity.contentType shouldBe ContentTypes.NoContentType
    sent(Option(List(thing))) shouldBe asJson(s"[$thingJson]")
  }

  test("what pekko marshals itself is left to it") {
    sent("text") shouldBe asText("text")
    sent(StatusCodes.OK -> "text") shouldBe asText("text")
    sent(Done: Done) shouldBe asText("")
    sent(Future.successful(Done: Done)) shouldBe asText("")
    sent(HttpEntity("raw")) shouldBe asText("raw")
    sent(StatusCodes.OK -> HttpEntity("raw")) shouldBe asText("raw")
    sent(HttpEntity("raw"): RequestEntity) shouldBe asText("raw")
    sent(FormData("a" -> "b")) shouldBe ((ContentTypes.`application/x-www-form-urlencoded`, "a=b"))
    sent(ByteString("b")) shouldBe ((ContentTypes.`application/octet-stream`, "b"))
    sent("b".getBytes) shouldBe ((ContentTypes.`application/octet-stream`, "b"))
    response(StatusCodes.NoContent).status shouldBe StatusCodes.NoContent
    response(StatusCodes.NoContent: StatusCode).status shouldBe StatusCodes.NoContent
    response(HttpResponse(StatusCodes.Accepted)).status shouldBe StatusCodes.Accepted
  }

  test("received") {
    received[Thing](thingJson) shouldBe thing
    received[List[Thing]](s"[$thingJson]") shouldBe List(thing)
    received[Shape]("""{"Empty":{}}""") shouldBe Empty
    received[Id]("\"x\"") shouldBe Id("x")
    received[Color]("\"red\"") shouldBe Color.Red
    Await.result(Unmarshal(HttpEntity("text")).to[String], 10.seconds) shouldBe "text"
    Await.result(Unmarshal(FormData("a" -> "b").toEntity).to[FormData], 10.seconds) shouldBe FormData("a" -> "b")
    Await.result(Unmarshal(HttpEntity("raw")).to[HttpEntity], 10.seconds) shouldBe HttpEntity("raw")
    Await.result(Unmarshal(HttpEntity("raw")).to[ByteString], 10.seconds) shouldBe ByteString("raw")
  }
}
