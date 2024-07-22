package pl.iterators.kebs.slick.enums

import enumeratum.values.{IntEnum, IntEnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import pl.iterators.kebs.enumeratum.KebsValueEnumeratum
import pl.iterators.kebs.slick.KebsSlickSupport
import slick.jdbc.PostgresProfile

class SlickMappedValueEnumColumnTypeTests extends AnyFunSuite with Matchers with KebsValueEnumeratum {
  object MyPostgresProfile extends PostgresProfile with KebsSlickSupport {
    override val api: APITagged = new APITagged {}
    trait APITagged extends JdbcAPI with KebsEnumImplicits
  }

  import MyPostgresProfile.api._

  sealed abstract class WorkerAccountStatusInt(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
  object WorkerAccountStatusInt extends IntEnum[WorkerAccountStatusInt] {
    case object Unapproved extends WorkerAccountStatusInt(0)
    case object Active     extends WorkerAccountStatusInt(1)
    case object Blocked    extends WorkerAccountStatusInt(2)

    override val values = findValues
  }

  test("MappedColumnType for value enum entries") {
    "implicitly[BaseColumnType[WorkerAccountStatusInt]]" should compile
  }

  test("Slick mapping") {
    """
      |class ATable(tag: Tag) extends Table[(Long, String, WorkerAccountStatusInt)](tag, "A_TABLE") {
      |      def id       = column[Long]("id")
      |      def name     = column[String]("name")
      |      def status   = column[WorkerAccountStatusInt]("status")
      |
      |      override def * = (id, name, status)
      |}
    """.stripMargin should compile
  }
}
