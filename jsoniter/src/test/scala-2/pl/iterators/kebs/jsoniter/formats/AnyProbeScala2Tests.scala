package pl.iterators.kebs.jsoniter.formats

import org.apache.pekko.http.scaladsl.marshalling.{ToEntityMarshaller, ToResponseMarshallable}
import org.apache.pekko.http.scaladsl.model._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.model._

object AnyProbeScala2Protocol extends JsoniterMarshallingGlue with KebsJsoniter with CaseClass1ToValueClass

class AnyProbeScala2Tests extends AnyFunSuite with Matchers {
  import AnyProbeScala2Protocol._

  // On Scala 2 the universal implicit fallback IS the contract: scalac pins the contravariant
  // type variable, so these probes must keep compiling with zero declared codecs.
  test("implicit fallback drives pekko marshaller resolution (Scala 2 contract)") {
    val entityMarshaller: ToEntityMarshaller[Seq[D]] = implicitly
    val response: ToResponseMarshallable             = StatusCodes.OK -> Seq(D(1, "a"))
    entityMarshaller should not be null
    response should not be null
  }
}
