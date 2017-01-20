import enumeratum.{Enum, EnumEntry}
import org.scalatest.{FunSuite, Matchers}

class SlickMappedEnumColumnTypeTests extends FunSuite with Matchers {
  import slick.driver.PostgresDriver.api._
  import slick.lifted.ProvenShape
  import pl.iterators.kebs.enums.lowercase._

  sealed trait WorkerAccountStatus extends EnumEntry
  object WorkerAccountStatus extends Enum[WorkerAccountStatus] {
    case object Unapproved extends WorkerAccountStatus
    case object Active     extends WorkerAccountStatus
    case object Blocked    extends WorkerAccountStatus

    override val values = findValues
  }

  test("MappedColumnType for enum entires") {
    "implicitly[BaseColumnType[WorkerAccountStatus]]" should compile
  }

  test("Slick mapping") {
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
