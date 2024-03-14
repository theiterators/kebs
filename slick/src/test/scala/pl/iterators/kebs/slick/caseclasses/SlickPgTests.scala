package pl.iterators.kebs.slick.caseclasses

import com.github.tminglei.slickpg._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class SlickPgTests extends AnyFunSuite with Matchers {

  import pl.iterators.kebs.slick.Kebs
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
  case class TestNumeric(value: Int)
  case class TestBool(value: Boolean)
  case class Test(id: TestId, string: TestString, num: TestNumeric)

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") {
    import driver.api._

    def id     = column[TestId]("id")
    def string = column[TestString]("string")
    def num    = column[TestNumeric]("num")
    def flag   = column[TestBool]("flag")

    override def * : ProvenShape[Test] = (id, string, num) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("String column extension methods") {
    """
      |class TestRepository1 {
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

  test("Numeric column extension methods") {
    """
      |class TestRepository2 {
      |  import PostgresDriver.api._
      |
      |  def power: DBIOAction[Seq[TestNumeric], NoStream, Effect.Read] =
      |    tests.map(t => t.num * t.num).result
      |  def mult2: DBIOAction[Seq[TestNumeric], NoStream, Effect.Read] =
      |    tests.map(_.num * 2).result
      |  def lt0: DBIOAction[Seq[Boolean], NoStream, Effect.Read] =
      |    tests.map(t => t.num <= 0).result
      |  def abs: DBIOAction[Seq[TestNumeric], NoStream, Effect.Read] =
      |    tests.map(t => t.num.abs).result
      |
      |  private val tests = TableQuery[Tests]
      |}
      """.stripMargin should compile
  }

  test("Boolean column extension methods") {
    """
      |class TestRepository2 {
      |  import PostgresDriver.api._
      |
      |  def and: DBIOAction[Seq[Boolean], NoStream, Effect.Read] =
      |    tests.map(t => t.flag && (t.num <= 0)).result
      |  def or: DBIOAction[Seq[Boolean], NoStream, Effect.Read] =
      |    tests.map(t => t.flag || (t.num > 0)).result
      |
      |  private val tests = TableQuery[Tests]
      |}
      """.stripMargin should compile
  }
}
