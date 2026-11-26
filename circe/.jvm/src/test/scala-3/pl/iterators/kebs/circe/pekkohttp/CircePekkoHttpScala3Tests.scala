package pl.iterators.kebs.circe.pekkohttp

import org.apache.pekko.Done
import org.apache.pekko.http.scaladsl.model.StatusCodes

class CircePekkoHttpScala3Tests extends CircePekkoHttpTestBase {
  import CircePekkoHttpTestDomain.Protocol._
  import system.dispatcher

  test("Done is left to pekko, as in kebs 2 on Scala 3") {
    sent(Done: Done) shouldBe ((plainText, ""))
    sent(StatusCodes.OK -> (Done: Done)) shouldBe ((plainText, ""))
  }
}
