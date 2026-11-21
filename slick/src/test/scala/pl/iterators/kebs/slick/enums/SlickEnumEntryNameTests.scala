package pl.iterators.kebs.slick.enums

import com.github.tminglei.slickpg._
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.slick.KebsSlickSupport

object SlickEnumEntryNameTests {
  sealed trait Status extends EnumEntry
  object Status       extends Enum[Status] {
    case object Active   extends Status { override val entryName = "is-active" }
    case object Inactive extends Status
    val values = findValues
  }
}

class SlickEnumEntryNameTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import SlickEnumEntryNameTests._

  trait PostgresDriver extends ExPostgresProfile with PgArraySupport with KebsSlickSupport {
    override val api: EnumAPI = new EnumAPI {}
    trait EnumAPI extends ExtPostgresAPI with ArrayImplicits with KebsBasicImplicits with KebsEnumImplicits
  }
  object PostgresDriver extends PostgresDriver

  trait UppercasePostgresDriver extends ExPostgresProfile with PgArraySupport with KebsSlickSupport {
    override val api: EnumAPI = new EnumAPI {}
    trait EnumAPI extends ExtPostgresAPI with ArrayImplicits with KebsBasicImplicits with KebsUppercaseEnumImplicits
  }
  object UppercasePostgresDriver extends UppercasePostgresDriver

  private def mapped[T, U](driver: ExPostgresProfile)(columnType: Any) =
    columnType.asInstanceOf[driver.MappedJdbcType[T, U]]

  test("enum columns use entryName") {
    import PostgresDriver.api._
    val column = mapped[Status, String](PostgresDriver)(implicitly[BaseColumnType[Status]])
    column.map(Status.Active) shouldBe "is-active"
    column.comap("is-active") shouldBe Status.Active
  }

  test("enum List and Seq columns use entryName") {
    import PostgresDriver.api._
    val list = mapped[List[Status], List[String]](PostgresDriver)(implicitly[BaseColumnType[List[Status]]])
    list.map(List(Status.Active, Status.Inactive)) shouldBe List("is-active", "Inactive")
    list.comap(List("is-active")) shouldBe List(Status.Active)

    val seq = mapped[Seq[Status], List[String]](PostgresDriver)(implicitly[BaseColumnType[Seq[Status]]])
    seq.map(Seq(Status.Active)) shouldBe List("is-active")
    seq.comap(List("is-active", "Inactive")) shouldBe Seq(Status.Active, Status.Inactive)
  }

  test("enum hstore entries use entryName") {
    import PostgresDriver.api._
    val hstore = implicitly[PostgresDriver.ToFromStringForHstore[Status]]
    hstore.to(Status.Active) shouldBe "is-active"
    hstore.from("is-active") shouldBe Status.Active
  }

  test("uppercase enum columns use entryName") {
    import UppercasePostgresDriver.api._
    val column = mapped[Status, String](UppercasePostgresDriver)(implicitly[BaseColumnType[Status]])
    column.map(Status.Active) shouldBe "IS-ACTIVE"
    column.comap("IS-ACTIVE") shouldBe Status.Active

    val seq = mapped[Seq[Status], List[String]](UppercasePostgresDriver)(implicitly[BaseColumnType[Seq[Status]]])
    seq.map(Seq(Status.Active)) shouldBe List("IS-ACTIVE")
  }
}
