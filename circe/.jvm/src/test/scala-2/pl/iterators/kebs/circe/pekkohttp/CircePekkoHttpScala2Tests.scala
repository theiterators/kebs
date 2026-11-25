package pl.iterators.kebs.circe.pekkohttp

import org.apache.pekko.Done
import org.apache.pekko.http.scaladsl.model.StatusCodes

class CircePekkoHttpScala2Tests extends CircePekkoHttpTestBase {
  import CircePekkoHttpTestDomain.Protocol._
  import system.dispatcher

  test("Done is sent as JSON, as in kebs 2 on Scala 2") {
    sent(Done: Done) shouldBe ((json, """{"Done":{}}"""))
    sent(StatusCodes.OK -> (Done: Done)) shouldBe ((json, """{"Done":{}}"""))
  }
}
