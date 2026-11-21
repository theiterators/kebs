package pl.iterators.kebs.slick.enums

import com.github.tminglei.slickpg.ExPostgresProfile
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.slick.KebsSlickSupport
import pl.iterators.kebs.testkit.model.Color

class SlickEnumNamesTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  trait DefaultDriver extends ExPostgresProfile with KebsSlickSupport {
    override val api: EnumAPI = new EnumAPI {}
    trait EnumAPI extends ExtPostgresAPI with KebsBasicImplicits with KebsEnumImplicits
  }
  object DefaultDriver extends DefaultDriver

  trait UppercaseDriver extends ExPostgresProfile with KebsSlickSupport {
    override val api: EnumAPI = new EnumAPI {}
    trait EnumAPI extends ExtPostgresAPI with KebsBasicImplicits with KebsUppercaseEnumImplicits
  }
  object UppercaseDriver extends UppercaseDriver

  trait LowercaseDriver extends ExPostgresProfile with KebsSlickSupport {
    override val api: EnumAPI = new EnumAPI {}
    trait EnumAPI extends ExtPostgresAPI with KebsBasicImplicits with KebsLowercaseEnumImplicits
  }
  object LowercaseDriver extends LowercaseDriver

  private def mapped[T, U](driver: ExPostgresProfile)(columnType: Any) = columnType.asInstanceOf[driver.MappedJdbcType[T, U]]

  test("enum names stored and read back") {
    import DefaultDriver.api._
    val column = mapped[Color, String](DefaultDriver)(implicitly[BaseColumnType[Color]])
    column.map(Color.DarkBlue) shouldBe "DarkBlue"
    column.comap("DarkBlue") shouldBe Color.DarkBlue
  }

  test("uppercase enum names stored and read back") {
    import UppercaseDriver.api._
    val column = mapped[Color, String](UppercaseDriver)(implicitly[BaseColumnType[Color]])
    column.map(Color.DarkBlue) shouldBe "DARKBLUE"
    column.comap("DARKBLUE") shouldBe Color.DarkBlue
  }

  test("lowercase enum names stored and read back") {
    import LowercaseDriver.api._
    val column = mapped[Color, String](LowercaseDriver)(implicitly[BaseColumnType[Color]])
    column.map(Color.DarkBlue) shouldBe "darkblue"
    column.comap("darkblue") shouldBe Color.DarkBlue
  }

  test("an EnumNaming in scope renames stored names") {
    import DefaultDriver.api._
    implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase
    val column                          = mapped[Color, String](DefaultDriver)(implicitly[BaseColumnType[Color]])
    column.map(Color.DarkBlue) shouldBe "DARK_BLUE"
    column.comap("dark_blue") shouldBe Color.DarkBlue
  }

  test("names are read case-insensitively") {
    import DefaultDriver.api._
    mapped[Color, String](DefaultDriver)(implicitly[BaseColumnType[Color]]).comap("darkblue") shouldBe Color.DarkBlue
  }
}
