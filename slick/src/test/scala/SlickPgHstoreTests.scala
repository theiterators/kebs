import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgHStoreSupport}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import slick.lifted.ProvenShape

import java.util.UUID

class SlickPgHstoreTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.hstore.KebsPgHStoreSupport

  trait PostgresDriver extends ExPostgresProfile with PgArraySupport with KebsPgHStoreSupport {
    override val api: HstoreAPI = new HstoreAPI {}
    trait HstoreAPI extends super.API with ArrayImplicits with HStoreImplicits
  }
  object PostgresDriver extends PostgresDriver

  abstract class BaseTable[T](tag: BaseTable.Tag, tableName: String) extends BaseTable.driver.Table[T](tag, tableName) {
    protected val driver: PostgresDriver = BaseTable.driver
  }

  object BaseTable {
    protected val driver = PostgresDriver
    type Tag = driver.api.Tag
  }

  case class Test(id: UUID, history: Map[String, String])

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") {
    import driver.api._

    def id: Rep[UUID]                     = column[UUID]("id")
    def history: Rep[Map[String, String]] = column[Map[String, String]]("history")

    override def * : ProvenShape[Test] =
      (id, history) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("String column extension methods") {
    class TestRepository1 {
      import PostgresDriver.api._

      def getValueFromKey(key: String) =
        tests.map(_.history +> key).result

      private val tests = TableQuery[Tests]
    }
  }
}
