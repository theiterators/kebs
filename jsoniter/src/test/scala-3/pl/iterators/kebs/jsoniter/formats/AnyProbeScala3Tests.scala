package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import org.apache.pekko.http.scaladsl.marshalling.{ToEntityMarshaller, ToResponseMarshallable}
import org.apache.pekko.http.scaladsl.model._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.model._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object AnyProbeProtocol extends JsoniterMarshallingGlue with KebsJsoniter with CaseClass1ToValueClass

/** The failure mode under regression: a universal implicit codec (what the pre-rewrite Scala 3 KebsJsoniter shipped) lets the
  * contravariant marshaller search maximize its type variable to `Any` and abort inside JsonCodecMaker.make[Any].
  */
object AnyProbeBrokenProtocol extends JsoniterMarshallingGlue {
  inline implicit def universalFallback[A]: JsonValueCodec[A] =
    JsonCodecMaker.make[A](CodecMakerConfig.withAllowRecursiveTypes(true))
}

final case class ProbeUser(c: C, i: Int)
object ProbeUser {
  implicit val codec: JsonValueCodec[ProbeUser]         = AnyProbeProtocol.deriveCodec
  implicit val seqCodec: JsonValueCodec[Seq[ProbeUser]] = AnyProbeProtocol.deriveCodec
}

class AnyProbeScala3Tests extends AnyFunSuite with Matchers {
  import AnyProbeProtocol._

  test("[probe 1] entity marshaller resolves via companion codec") {
    val entityMarshaller: ToEntityMarshaller[Seq[ProbeUser]] = implicitly
    entityMarshaller should not be null
  }

  test("[probe 2] StatusCodes.OK -> value marshals as application/json") {
    val response: ToResponseMarshallable = StatusCodes.OK -> Seq(ProbeUser(C(1), 2))
    val resp                             = Await.result(response(HttpRequest()), 5.seconds)
    resp.entity.contentType.mediaType shouldBe MediaTypes.`application/json`
  }

  test("[probe 3] non-interference: plain String stays text/plain with the trait in scope") {
    val response: ToResponseMarshallable = StatusCodes.OK -> "hello"
    val resp                             = Await.result(response(HttpRequest()), 5.seconds)
    resp.entity.contentType.mediaType shouldBe MediaTypes.`text/plain`
  }

  test("[probe 4] non-interference: raw HttpEntity still marshals") {
    val response: ToResponseMarshallable = StatusCodes.OK -> HttpEntity("raw")
    val resp                             = Await.result(response(HttpRequest()), 5.seconds)
    resp.status shouldBe StatusCodes.OK
  }

  test("a universal implicit codec breaks every marshalling site (the Any bug, must not compile)") {
    assertDoesNotCompile(
      """
      import AnyProbeBrokenProtocol._
      implicitly[ToEntityMarshaller[Seq[D]]]
      """
    )
    assertDoesNotCompile(
      """
      import AnyProbeBrokenProtocol._
      val r: ToResponseMarshallable = StatusCodes.OK -> Seq(D(1, "a"))
      """
    )
  }
}
