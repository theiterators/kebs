import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.json.KebsSpray
import pl.iterators.kebs.tag.meta.tagged
import pl.iterators.kebs.tagged._
import util.Properties.versionNumberString
import spray.json.{DefaultJsonProtocol, JsonFormat}

package object domain extends Domain {}

@tagged trait Domain {
  trait SomeTag
  type SomeTaggedValue = Int @@ SomeTag
}

object dto {
  import domain._
  case class SomeDto(opt: Option[SomeTaggedValue])
}

class SprayKebsIssue47Test extends AnyFunSuite with Matchers with DefaultJsonProtocol with KebsSpray {
  import dto._

  test("diverging implicit bug fixed in 2.13.1") {
    if (versionNumberString.startsWith("2.13")) {
      """implicitly[JsonFormat[Option[SomeDto]]]""".stripMargin should compile
    } else {
      """implicitly[JsonFormat[Option[SomeDto]]]""".stripMargin shouldNot compile
    }
  }
}
