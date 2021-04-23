package hstore

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgHStoreSupport}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import slick.lifted.ProvenShape

import java.util.UUID

class SlickPgHstoreTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.Kebs

  trait PostgresDriver extends ExPostgresProfile with PgArraySupport with PgHStoreSupport {
    override val api: HstoreAPI = new HstoreAPI {}
    trait HstoreAPI extends super.API with ArrayImplicits with HStoreImplicits with Kebs
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
  case class TestKey(value: String)
  case class TestValue(value: String)
  case class Test(id: TestId, hstoreMap: Map[TestKey, TestValue])

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") {
    import driver.api._

    def id: Rep[TestId]                         = column[TestId]("id")
    def hstoreMap: Rep[Map[TestKey, TestValue]] = column[Map[TestKey, TestValue]]("hstore_map")

    override def * : ProvenShape[Test] = (id, hstoreMap) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("Case class hstore extension methods") {
    """
      |class TestRepository1 {
      |      import PostgresDriver.api._
      |
      |      def exists(key: TestKey) =
      |        tests.map(_.hstoreMap ?? key).result
      |
      |      private val tests = TableQuery[Tests]
      |}""".stripMargin should compile
  }
}
