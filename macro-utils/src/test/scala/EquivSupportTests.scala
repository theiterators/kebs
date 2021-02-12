import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EquivSupportTests extends AnyFunSuite with Matchers {

  import pl.iterators.kebs.support._
  import StringsDomain._

  private def areEquiv[A](e1: A, e2: A)(implicit E: Equiv[A]): Boolean =
    E.equiv(e1, e2)

  implicit private val equiv: Equiv[String] = Equiv.reference[String]

  private val stringValue = "value"

  test("Equiv should be available for tagged types with Equiv implementation") {
    val string       = new String(stringValue)
    val stringTagged = TaggedString(string)

    areEquiv(stringTagged, stringTagged) shouldBe true
    areEquiv(stringTagged, TaggedString(string)) shouldBe true
    areEquiv(stringTagged, TaggedString(new String(stringValue))) shouldBe false
  }

  test("Equiv should be available for boxed types with Equiv implementation") {
    val string      = new String(stringValue)
    val stringBoxed = BoxedString(string)

    areEquiv(stringBoxed, stringBoxed) shouldBe true
    areEquiv(stringBoxed, BoxedString(string)) shouldBe true
    areEquiv(stringBoxed, BoxedString(new String(stringValue))) shouldBe false
  }
}
