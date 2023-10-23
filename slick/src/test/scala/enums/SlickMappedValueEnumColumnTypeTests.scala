package enums

import enumeratum.values.{IntEnum, IntEnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsValueEnumeratum

class SlickMappedValueEnumColumnTypeTests extends AnyFunSuite with Matchers with KebsValueEnumeratum {
  import slick.jdbc.PostgresProfile.api._
  import pl.iterators.kebs.enums._

  sealed abstract class WorkerAccountStatusInt(val value: Int) extends IntEnumEntry
  object WorkerAccountStatusInt extends IntEnum[WorkerAccountStatusInt] {
    case object Unapproved extends WorkerAccountStatusInt(0)
    case object Active     extends WorkerAccountStatusInt(1)
    case object Blocked    extends WorkerAccountStatusInt(2)

    override val values = findValues
  }

  // TODO: FIX THIS TEST
  test("MappedColumnType for value enum entries") {
    val x = implicitly[BaseColumnType[WorkerAccountStatusInt]] // this variable is just to show implicit hints for debug
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
