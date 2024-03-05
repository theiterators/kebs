package enums

import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumLike
import slick.lifted.Isomorphism
import pl.iterators.kebs.enumeratum.KebsEnumeratum

class EnumIsomorphismTests extends AnyFunSuite with Matchers with KebsEnumeratum {

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting

    val values = findValues
  }

  import Greeting._

  test("Implicit isomorphism for EnumEntry") {
    import pl.iterators.kebs.slick.enums._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(Hello) shouldBe "Hello"
    iso.comap("Hello") shouldBe Hello
  }

  test("Implicit isomorphism for EnumEntry - lowercase") {
    import pl.iterators.kebs.slick.enums.lowercase._

    val enumm = new EnumLike[Greeting] {
      override def values: Seq[Greeting] = Greeting.values}

    val iso = implicitly[Isomorphism[Greeting, String]]

    iso.map(GoodBye) shouldBe "goodbye"
    iso.comap("goodbye") shouldBe GoodBye
  }

  test("Implicit isomorphism for EnumEntry - uppercase") {
    import pl.iterators.kebs.slick.enums.uppercase._

    val iso = implicitly[Isomorphism[Greeting, String]]
    iso.map(GoodBye) shouldBe "GOODBYE"
    iso.comap("GOODBYE") shouldBe GoodBye
  }

}
