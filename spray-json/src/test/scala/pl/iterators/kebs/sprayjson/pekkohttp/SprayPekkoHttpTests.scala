package pl.iterators.kebs.sprayjson.pekkohttp

import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object SprayPekkoHttpTestDomain {
  object Protocol extends DefaultJsonProtocol with SprayJsonSupport with KebsSprayJson

  case class Thing(a: Int, b: String)
  sealed trait Shape
  case class Circle(r: Double, label: String) extends Shape
  case object Empty                           extends Shape
}

abstract class SprayPekkoHttpTestBase extends AnyFunSuite with Matchers with BeforeAndAfterAll {
  implicit protected val system: ActorSystem = ActorSystem("SprayPekkoHttpTests")

  override protected def afterAll(): Unit = Await.result(system.terminate(), 10.seconds): Unit

  protected def sent(response: ToResponseMarshallable): (ContentType, String) = {
    import system.dispatcher
    val entity = Await.result(response(HttpRequest()).flatMap(_.entity.toStrict(5.seconds)), 10.seconds)
    (entity.contentType, entity.data.utf8String)
  }

  protected def received[A: FromEntityUnmarshaller](entity: HttpEntity): A = Await.result(Unmarshal(entity).to[A], 10.seconds)

  protected val plainText: ContentType = ContentTypes.`text/plain(UTF-8)`
  protected val json: ContentType      = ContentTypes.`application/json`
}

class SprayPekkoHttpTests extends SprayPekkoHttpTestBase {
  import SprayPekkoHttpTestDomain._
  import Protocol._
  import system.dispatcher

  test("case classes and sealed hierarchies are sent as JSON") {
    sent(Thing(1, "x")) shouldBe ((json, """{"a":1,"b":"x"}"""))
    sent(StatusCodes.Created -> Thing(1, "x")) shouldBe ((json, """{"a":1,"b":"x"}"""))
    sent(Future.successful(Option(Thing(1, "x")))) shouldBe ((json, """{"a":1,"b":"x"}"""))
    sent(Circle(1.5, "c"): Shape) shouldBe ((json, """{"Circle":{"label":"c","r":1.5}}"""))
    received[Shape](HttpEntity(ContentTypes.`application/json`, """{"Empty":{}}""")) shouldBe Empty
  }

  test("what pekko marshals itself is left to it") {
    sent("text") shouldBe ((plainText, "text"))
    sent(Done: Done) shouldBe ((plainText, ""))
    sent(Future.successful(Done: Done)) shouldBe ((plainText, ""))
    sent(StatusCodes.OK -> (Done: Done)) shouldBe ((plainText, ""))
    sent(HttpEntity("raw"): RequestEntity) shouldBe ((plainText, "raw"))
    sent(HttpEntity("raw"): UniversalEntity) shouldBe ((plainText, "raw"))
    received[HttpEntity](HttpEntity("raw")) shouldBe HttpEntity("raw")
  }
}
