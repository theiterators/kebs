package pl.iterators.kebs.sprayjson.pekkohttp

import org.apache.pekko.http.scaladsl.model._

// on Scala 3 pekko's case classes do not compile next to a kebs protocol (as in kebs 2)
class SprayPekkoHttpScala2Tests extends SprayPekkoHttpTestBase {
  import SprayPekkoHttpTestDomain.Protocol._
  import system.dispatcher

  test("pekko's case classes are left to it") {
    sent(FormData("a" -> "b")) shouldBe ((ContentTypes.`application/x-www-form-urlencoded`, "a=b"))
    received[FormData](FormData("a" -> "b").toEntity) shouldBe FormData("a" -> "b")
    sent(HttpEntity("raw")) shouldBe ((plainText, "raw"))
  }
}
