package pl.iterators.kebs.circe.pekkohttp

import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce

import scala.concurrent.duration._
import scala.concurrent.Await

object CircePekkoHttpTestDomain {
  object Protocol extends FailFastCirceSupport with KebsCirce

  case class Thing(a: Int, b: String)
  sealed trait Shape
  case class Circle(r: Double, label: String) extends Shape
  case object Empty                           extends Shape
}

abstract class CircePekkoHttpTestBase extends AnyFunSuite with Matchers with BeforeAndAfterAll {
  implicit protected val system: ActorSystem = ActorSystem("CircePekkoHttpTests")

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

class CircePekkoHttpTests extends CircePekkoHttpTestBase {
  import CircePekkoHttpTestDomain._
  import Protocol._
  import system.dispatcher

  test("case classes and sealed hierarchies are sent as JSON") {
    sent(Thing(1, "x")) shouldBe ((json, """{"a":1,"b":"x"}"""))
    sent(StatusCodes.Created -> Thing(1, "x")) shouldBe ((json, """{"a":1,"b":"x"}"""))
    sent(Circle(1.5, "c"): Shape) shouldBe ((json, """{"Circle":{"r":1.5,"label":"c"}}"""))
    received[Shape](HttpEntity(ContentTypes.`application/json`, """{"Empty":{}}""")) shouldBe Empty
  }

  test("entities are left to pekko") {
    sent(HttpEntity("raw"): RequestEntity) shouldBe ((plainText, "raw"))
    sent(HttpEntity("raw"): UniversalEntity) shouldBe ((plainText, "raw"))
    received[HttpEntity](HttpEntity("raw")) shouldBe HttpEntity("raw")
  }
}
