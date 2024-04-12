import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EnumIsomorphismTests extends AnyFunSuite with Matchers {
  import slick.jdbc.PostgresProfile.api._

  sealed trait Greeting extends EnumEntry

  object Greeting extends Enum[Greeting] {
    case object Hello   extends Greeting
    case object GoodBye extends Greeting
    case object Hi      extends Greeting
    case object Bye     extends Greeting

    val values = findValues
  }

  import Greeting._
  import pl.iterators.kebs.enumeratum._

  test("Implicit isomorphism for EnumEntry") {
    import pl.iterators.kebs.slick.enums._
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Greeting, String]]
    iso.map(Hello) shouldBe "Hello"
    iso.comap("Hello") shouldBe Hello
  }

  test("Implicit isomorphism for EnumEntry - lowercase") {
    import pl.iterators.kebs.slick.enums.lowercase._

    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Greeting, String]]
    iso.map(GoodBye) shouldBe "goodbye"
    iso.comap("goodbye") shouldBe GoodBye
  }

  test("Implicit isomorphism for EnumEntry - uppercase") {
    import pl.iterators.kebs.slick.enums.uppercase._

    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Greeting, String]]
    iso.map(GoodBye) shouldBe "GOODBYE"
    iso.comap("GOODBYE") shouldBe GoodBye
  }

}
