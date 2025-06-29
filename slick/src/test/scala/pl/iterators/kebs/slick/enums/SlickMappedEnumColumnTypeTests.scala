package pl.iterators.kebs.slick.enums

import com.github.tminglei.slickpg.ExPostgresProfile
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum

class SlickMappedEnumColumnTypeTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import pl.iterators.kebs.slick.KebsSlickSupport

  trait PostgresDriver extends ExPostgresProfile with KebsSlickSupport {
    override val api: EnumAPI = new EnumAPI {}
    trait EnumAPI extends ExtPostgresAPI with KebsBasicImplicits with KebsValueClassLikeImplicits with KebsEnumImplicits
  }
  object PostgresDriver extends PostgresDriver

  abstract class BaseTable[T](tag: BaseTable.Tag, tableName: String) extends BaseTable.driver.Table[T](tag, tableName) {
    protected val driver: PostgresDriver = BaseTable.driver
  }

  object BaseTable {
    protected val driver = PostgresDriver
    type Tag = driver.api.Tag
  }

  sealed trait WorkerAccountStatus extends EnumEntry
  object WorkerAccountStatus       extends Enum[WorkerAccountStatus] {
    case object Unapproved extends WorkerAccountStatus
    case object Active     extends WorkerAccountStatus
    case object Blocked    extends WorkerAccountStatus

    override val values = findValues
  }
  import PostgresDriver.api._
  test("MappedColumnType for enum entries") {
    "implicitly[BaseColumnType[WorkerAccountStatus]]" should compile
  }

  test("Slick mapping") {
    class ATable(tag: BaseTable.Tag) extends BaseTable[(Long, String, WorkerAccountStatus)](tag, "A_TABLE") {
      import driver.api._
      def id     = column[Long]("id")
      def name   = column[String]("name")
      def status = column[WorkerAccountStatus]("status")

      override def * = (id, name, status)
    }
    """
      |class ATable(tag: Tag) extends Table[(Long, String, WorkerAccountStatus)](tag, "A_TABLE") {
      |      def id       = column[Long]("id")
      |      def name     = column[String]("name")
      |      def status   = column[WorkerAccountStatus]("status")
      |
      |      override def * = (id, name, status)
      |}
    """.stripMargin should compile
  }
}
