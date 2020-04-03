import java.util.UUID

import pl.iterators.kebs.json.{KebsSpray, noflat}
import spray.json._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class VersionSpecificTests extends AnyFunSuite with Matchers {
  object KebsProtocol extends DefaultJsonProtocol with KebsSpray
  import KebsProtocol._

  case class Request(region: String, currency: String, date: String, code: String, items: List[RequestItem])
  case class RequestItem(itemCode: String, language: String, answers: Option[List[Answer]], requirements: String, travellers: String)
  case class Answer(answer: String)

  test("issue #11") {
    "implicitly[JsonFormat[Request]]" should compile
  }

}
