import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PartialOrderingSupportTests extends AnyFunSuite with Matchers {

  import pl.iterators.kebs.support._
  import StringsDomain._

  private def maybeCompare[A](e1: A, e2: A)(implicit PO: PartialOrdering[A]): Option[Int] =
    PO.tryCompare(e1, e2)

  implicit private val partialOrdering: PartialOrdering[String] = new PartialOrdering[String] {
    private val maxLength = 10
    override def tryCompare(x: String, y: String): Option[Int] = (x.length, y.length) match {
      case (lx, ly) if (lx < maxLength && ly < maxLength) => Some(x.compareTo(y))
      case _                                              => None
    }
    override def lteq(x: String, y: String): Boolean = (x.length, y.length) match {
      case (lx, ly) if (lx < maxLength && ly < maxLength) => x < y
      case _                                              => false
    }
  }

  private val shorterString = "1"
  private val shortString   = "123"
  private val longString    = "12345678901234"

  test("Equiv should be available for tagged types with Equiv implementation") {
    maybeCompare(TaggedString(shorterString), TaggedString(shortString)) shouldBe defined
    maybeCompare(TaggedString(longString), TaggedString(shortString)) shouldBe None
    maybeCompare(TaggedString(shorterString), TaggedString(longString)) shouldBe None
  }

  test("Equiv should be available for boxed types with Equiv implementation") {
    maybeCompare(BoxedString(shorterString), BoxedString(shortString)) shouldBe defined
    maybeCompare(BoxedString(longString), BoxedString(shortString)) shouldBe None
    maybeCompare(BoxedString(shorterString), BoxedString(longString)) shouldBe None
  }
}
