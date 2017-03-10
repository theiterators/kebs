import enumeratum.values.{IntEnum, IntEnumEntry}
import org.scalatest.{FunSuite, Matchers}
import slick.lifted.Isomorphism

class ValueEnumIsomorphismTests extends FunSuite with Matchers {

  sealed abstract class IntGreeting(val value: Int) extends IntEnumEntry

  object IntGreeting extends IntEnum[IntGreeting] {
    val values = findValues

    case object Hello   extends IntGreeting(0)
    case object GoodBye extends IntGreeting(1)
    case object Hi      extends IntGreeting(2)
    case object Bye     extends IntGreeting(3)
  }

  import IntGreeting._

  test("implicit isomorphism from ValueEnumEntry") {
    import pl.iterators.kebs.enums._

    val iso = implicitly[Isomorphism[IntGreeting, Int]]
    iso.map(Bye) shouldBe 3
    iso.comap(3) shouldBe Bye
  }

}
