import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgHStoreSupport}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.YearMonthString
import slick.lifted.{ProvenShape, Rep}

import java.time.YearMonth
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

  case class Test(id: UUID, history: Map[YearMonth, String])

  class Tests(tag: BaseTable.Tag) extends BaseTable[Test](tag, "test") with YearMonthString {
    import driver.api._

    def id: Rep[UUID]                        = column[UUID]("id")
    def history: Rep[Map[YearMonth, String]] = column[Map[YearMonth, String]]("history")

    override def * : ProvenShape[Test] = (id, history) <> ((Test.apply _).tupled, Test.unapply)
  }

  test("Hstore extension methods") {
    class TestRepository1 {
      import PostgresDriver.api._

      def exists(key: YearMonth) =
        tests.map(_.history ?? key).result

      private val tests = TableQuery[Tests]
    }
  }
}
