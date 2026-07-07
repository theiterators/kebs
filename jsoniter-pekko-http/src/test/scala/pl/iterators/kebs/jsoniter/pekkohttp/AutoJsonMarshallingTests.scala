package pl.iterators.kebs.jsoniter.pekkohttp

import com.github.pjfanning.pekkohttpjsoniterscala.JsoniterScalaSupport
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.{ToEntityMarshaller, ToResponseMarshallable}
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.jsoniter.{KebsJsoniter, KebsJsoniterSnakified}
import pl.iterators.kebs.opaque.Opaque

import java.net.URI
import java.time.Instant
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration._

opaque type BarId = UUID
object BarId extends Opaque[BarId, UUID]

opaque type BarSeenAt <: Instant = Instant
object BarSeenAt extends Opaque[BarSeenAt, Instant]

// NO companion codec anywhere — that's the point of the auto module
final case class Bar(id: BarId, seenAt: BarSeenAt, homepage: URI)

object SnakifiedBazProtocol extends KebsJsoniterSnakified

// a type WITH a companion codec, in a deliberately different (snakified) format:
// the wire format proves whether the companion codec or a fresh derivation was used
final case class Baz(someField: Int)
object Baz {
  implicit val codec: JsonValueCodec[Baz] = SnakifiedBazProtocol.deriveCodec
}

object AutoProtocol extends JsoniterScalaSupport with KebsJsoniter with URIString with AutoJsonMarshalling

class AutoJsonMarshallingTests extends AnyFunSuite with Matchers with BeforeAndAfterAll {
  import AutoProtocol._

  implicit val system: ActorSystem = ActorSystem("auto-json-marshalling-tests")
  import system.dispatcher

  override def afterAll(): Unit = {
    Await.ready(system.terminate(), 10.seconds)
    ()
  }

  val bar = Bar(
    BarId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
    BarSeenAt(Instant.parse("2026-02-02T00:00:00Z")),
    new URI("https://iterators.pl")
  )
  val barJson =
    "{\"id\":\"00000000-0000-0000-0000-000000000002\",\"seenAt\":\"2026-02-02T00:00:00Z\",\"homepage\":\"https://iterators.pl\"}"

  private def render(r: ToResponseMarshallable): HttpResponse = Await.result(r(HttpRequest()), 5.seconds)
  private def bodyOf(resp: HttpResponse): String              =
    Await.result(resp.entity.toStrict(5.seconds), 5.seconds).data.utf8String

  test("zero-declaration entity marshaller resolves") {
    val m: ToEntityMarshaller[Seq[Bar]] = implicitly
    m should not be null
  }

  test("zero-declaration marshalling: OK -> value with no companion codec") {
    val resp = render(StatusCodes.OK -> bar)
    resp.status shouldBe StatusCodes.OK
    resp.entity.contentType.mediaType shouldBe MediaTypes.`application/json`
    bodyOf(resp) shouldBe barJson
  }

  test("zero-declaration unmarshalling (the entity(as[Bar]) path)") {
    val entity = HttpEntity(ContentTypes.`application/json`, barJson)
    Await.result(Unmarshal(entity).to[Bar], 5.seconds) shouldBe bar
  }

  test("companion codec is preferred over a fresh derivation") {
    val resp = render(StatusCodes.OK -> Baz(7))
    bodyOf(resp) shouldBe "{\"some_field\":7}"
  }

  test("sharp edge (documented): plain String is hijacked to application/json") {
    val resp = render(StatusCodes.OK -> "hello")
    resp.entity.contentType.mediaType shouldBe MediaTypes.`application/json`
    bodyOf(resp) shouldBe "\"hello\""
  }

  test("sharp edge (documented): raw HttpEntity stops compiling in mixed-in scopes") {
    assertDoesNotCompile("""val r: ToResponseMarshallable = StatusCodes.OK -> HttpEntity("raw")""")
  }
}
