package pl.iterators.kebs.slick.arrays

import com.github.tminglei.slickpg._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import slick.lifted.ProvenShape

import java.time.YearMonth
import java.util.UUID

class SlickPgArrayTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.instances.time.YearMonthString
  import pl.iterators.kebs.slick.Kebs

  trait PostgresDriver extends ExPostgresProfile with PgArraySupport {
    override val api: ArrayAPI = new ArrayAPI {}
    trait ArrayAPI extends super.API with ArrayImplicits with Kebs with YearMonthString
  }
  object PostgresDriver extends PostgresDriver

  abstract class BaseTable[T](tag: BaseTable.Tag, tableName: String) extends BaseTable.driver.Table[T](tag, tableName) {
    protected val driver: PostgresDriver = BaseTable.driver
  }

  object BaseTable {
    protected val driver = PostgresDriver
    type Tag = driver.api.Tag
  }

  case class TestId(value: UUID)
  case class TestCC(value: Int)
  case class Test(id: TestId, ccList: List[TestCC])

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") {
    import driver.api._

    def id     = column[TestId]("id")
    def ccList = column[List[TestCC]]("cc_list")

    override def * : ProvenShape[Test] = (id, ccList) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("No ValueClassLike implicits derived") {
    import pl.iterators.kebs.core.macros.ValueClassLike

    "implicitly[ValueClassLike[YearMonth, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, YearMonth]]" shouldNot typeCheck
  }

  test("Case class list extension methods") {
    """
      |class TestRepository1 {
      |      import PostgresDriver.api._
      |
      |      def contains(arr: List[TestCC]) =
      |        tests.map(_.ccList @> arr)
      |
      |      private val tests = TableQuery[Tests]
      |}
      |""".stripMargin should compile
  }

  case class ObjectTest(id: TestId, objList: List[YearMonth])

  class ObjectTests(tag: BaseTable.Tag) extends BaseTable[ObjectTest](tag, "test") {
    import driver.api._

    def id      = column[TestId]("id")
    def objList = column[List[YearMonth]]("obj_list")

    override def * : ProvenShape[ObjectTest] = (id, objList) <> ((ObjectTest.apply _).tupled, ObjectTest.unapply)
  }

  test("Object list extension methods") {
    """
      |class TestRepository1 {
      |      import PostgresDriver.api._
      |
      |      def contains(arr: List[YearMonth]) =
      |        tests.map(_.objList @> arr)
      |
      |      private val tests = TableQuery[ObjectTests]
      |}
      |""".stripMargin should compile
  }
}
