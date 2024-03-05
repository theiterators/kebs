package pl.iterators.kebs.slick.enums

import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum

class SlickMappedEnumColumnTypeTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import slick.jdbc.PostgresProfile.api._
  import pl.iterators.kebs.slick.enums._

  sealed trait WorkerAccountStatus extends EnumEntry
  object WorkerAccountStatus extends Enum[WorkerAccountStatus] {
    case object Unapproved extends WorkerAccountStatus
    case object Active     extends WorkerAccountStatus
    case object Blocked    extends WorkerAccountStatus

    override val values = findValues
  }

  test("MappedColumnType for enum entries") {
    "implicitly[BaseColumnType[WorkerAccountStatus]]" should compile
  }

  test("Slick mapping") {
    class ATable(tag: Tag) extends Table[(Long, String, WorkerAccountStatus)](tag, "A_TABLE") {
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
