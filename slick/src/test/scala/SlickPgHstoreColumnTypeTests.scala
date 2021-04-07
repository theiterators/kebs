import com.github.tminglei.slickpg._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickPgHstoreColumnTypeTests extends AnyFunSuite with Matchers {
  case class CategoryName(name: String)
  case class CategoryImportance(value: Int)

  object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
    override val api: APIWithHStore = new APIWithHStore {}
    trait APIWithHStore extends super.API with HStoreImplicits
  }

  import MyPostgresProfile.api._
  import pl.iterators.kebs._

  test("Value classes to HStore mapping") {
    """
      | class HStoreTestTable(tag: Tag) extends Table[(Long, Map[CategoryName, CategoryImportance])](tag, "HStoreTestTable") {
      |   def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |   def categories = column[Map[CategoryName, CategoryImportance]]("categories")
      |
      |   def * = (id, categories)
      |}
    """.stripMargin should compile
  }

  test("Standard types to HStore mapping") {
    import java.time.YearMonth
    import pl.iterators.kebs.instances.TimeInstances.YearMonthString
    """
      | class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTestTable") with YearMonthString {
      |   def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |   def history: Rep[Map[YearMonth, Boolean]] = column[Map[YearMonth, Boolean]]("history")
      |
      |   def * = (id, history)
      |}
    """.stripMargin should compile
  }
}
