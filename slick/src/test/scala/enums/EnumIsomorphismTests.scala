package enums

import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import slick.lifted.Isomorphism

class EnumIsomorphismTests extends AnyFunSuite with Matchers {

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting

    val values = findValues
  }

  import Greeting._

  test("Implicit isomorphism from EnumEntry") {
    import pl.iterators.kebs.enums._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(Hello) shouldBe "Hello"
    iso.comap("Hello") shouldBe Hello
  }

  test("Implicit isomorphism from EnumEntry - lowercase") {
    import pl.iterators.kebs.enums.lowercase._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(GoodBye) shouldBe "goodbye"
    iso.comap("goodbye") shouldBe GoodBye
  }

  test("Implicit isomorphism from EnumEntry - uppercase") {
    import pl.iterators.kebs.enums.uppercase._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(GoodBye) shouldBe "GOODBYE"
    iso.comap("GOODBYE") shouldBe GoodBye
  }

}
