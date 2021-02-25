package pl.iterators.kebs.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.NetInstances.InetAddressString
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.net.InetAddress

// TODO (25/02/21) add tests
class NetInstancesTests
    extends AnyFunSuite
    with Matchers
    with DefaultJsonProtocol
    with KebsSpray
    with NetInstances
    with InetAddressString {

  test("InetAddress standard format") {
    val jf    = implicitly[JsonFormat[InetAddress]]
    val value = "iteratorshq.com"
    val obj   = InetAddress.getByName(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("InetAddress wrong format exception") {
    import NetInstances.InetAddressFormat

    val jf    = implicitly[JsonFormat[InetAddress]]
    val value = "NotAnInetAddress"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[InetAddress, String](classOf[InetAddress], value, InetAddressFormat))
  }
}
