import enumeratum.{Enum, EnumEntry}
import org.scalatest.{FunSuite, Matchers}
import slick.lifted.Isomorphism

class EnumIsomorphismTests extends FunSuite with Matchers {

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    val values = findValues

    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting
  }

  import Greeting._

  test("implicit isomorphism from EnumEntry") {
    import pl.iterators.kebs.enums._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(Hello) shouldBe "Hello"
    iso.comap("Hello") shouldBe Hello
  }

  test("implicit isomorphism from EnumEntry - lowercase") {
    import pl.iterators.kebs.enums.lowercase._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(GoodBye) shouldBe "goodbye"
    iso.comap("goodbye") shouldBe GoodBye
  }

  test("implicit isomorphism from EnumEntry - uppercase") {
    import pl.iterators.kebs.enums.uppercase._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(GoodBye) shouldBe "GOODBYE"
    iso.comap("GOODBYE") shouldBe GoodBye
  }

}
