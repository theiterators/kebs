package enums

import enumeratum.values.{IntEnum, IntEnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickMappedValueEnumColumnTypeTests extends AnyFunSuite with Matchers {
  import slick.jdbc.PostgresProfile.api._
  import pl.iterators.kebs.enums._

  sealed abstract class WorkerAccountStatusInt(val value: Int) extends IntEnumEntry
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
