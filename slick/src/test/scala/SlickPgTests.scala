import java.util.UUID

import com.github.tminglei.slickpg.ExPostgresProfile
import org.scalatest.{FunSuite, Matchers}

class SlickPgTests extends FunSuite with Matchers {
  import pl.iterators.kebs.Kebs
  import slick.lifted.ProvenShape

  case class ServiceLineName(name: String)
  case class Id(id: Int)
  case class ServiceLine(id: Id, name: ServiceLineName)

  trait PostgresDriver extends ExPostgresProfile {
    override val api = PostgresApi

    object PostgresApi extends API with Kebs
  }
  object PostgresDriver extends PostgresDriver

  abstract class BaseTable[T](tag: BaseTable.Tag, tableName: String) extends BaseTable.driver.Table[T](tag, tableName) {
    protected val driver: PostgresDriver = BaseTable.driver
  }
  object BaseTable {
    protected val driver = PostgresDriver
    type Tag = driver.api.Tag
  }

  test("Mapping with trait") {
    """
      |class ServiceLines(tag: BaseTable.Tag) extends BaseTable[ServiceLine](tag, "service_line") {
      |      import driver.api._
      |
      |      def id: Rep[Id] = column[Id]("id", O.PrimaryKey)
      |      def name: Rep[ServiceLineName] = column[ServiceLineName]("name")
      |
      |      override def * : ProvenShape[ServiceLine] = (id, name) <> (ServiceLine.tupled, ServiceLine.unapply)
      |    }
    """.stripMargin should compile
  }

  case class TestId(value: UUID)
  case class TestString(value: String)
  case class Test(id: TestId, string: TestString)

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") {
    import driver.api._

    def id     = column[TestId]("id")
    def string = column[TestString]("string")

    override def * : ProvenShape[Test] = (id, string) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("Column extension methods") {
    """
      |class TestRepository {
      |  import PostgresDriver.api._
      |
      |  def toLowerCase(ilikeString: String): DBIOAction[Seq[TestString], NoStream, Effect.Read] =
      |    tests.map(_.string.toLowerCase).result
      |  def filter(ilikeString: String): DBIOAction[Seq[Test], NoStream, Effect.Read] =
      |    tests.filter(_.string.toLowerCase.like(s"%$ilikeString%")).result
      |
      |  private val tests = TableQuery[Tests]
      |}
    """.stripMargin should compile
  }
}
