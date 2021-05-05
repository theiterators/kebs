package hstore

import com.github.tminglei.slickpg._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances.{DayOfWeekNumber, YearMonthString}

import java.time.{DayOfWeek, YearMonth}

class SlickPgHstoreColumnTypeTests extends AnyFunSuite with Matchers {
  case class CategoryName(name: String)
  case class CategoryImportance(value: String)

  import pl.iterators.kebs.Kebs

  object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
    override val api: APIWithHStore = new APIWithHStore {}
    trait APIWithHStore extends super.API with HStoreImplicits with Kebs
  }

  import MyPostgresProfile.api._

  test("Value classes to HStore mapping") {
    """
      |    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[CategoryName, CategoryImportance])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                                          = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[CategoryName, CategoryImportance]] = column[Map[CategoryName, CategoryImportance]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("String value to HStore mapping") {
    """
      |    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, String])](tag, "HStoreTestTable") with YearMonthString {
      |      def id                                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def history: Rep[Map[YearMonth, String]] = column[Map[YearMonth, String]]("history")
      |
      |      def * = (id, history)
      |    }
      |""".stripMargin should compile
  }

  test("Int value to HStore mapping") {
    """
      |    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[DayOfWeek, String])](tag, "HStoreTestTable") with DayOfWeekNumber {
      |      def id                                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def history: Rep[Map[DayOfWeek, String]] = column[Map[DayOfWeek, String]]("history")
      |
      |      def * = (id, history)
      |    }
    """.stripMargin should compile
  }

  test("Long value to HStore mapping") {
    """
      |    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Long])](tag, "HStoreTestTable") with YearMonthString {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def history: Rep[Map[YearMonth, Long]] = column[Map[YearMonth, Long]]("history")
      |
      |      def * = (id, history)
      |    }
      |""".stripMargin
  }

  test("Boolean value to HStore mapping") {

    """
      |    class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTestTable") with YearMonthString {
      |      def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def history: Rep[Map[YearMonth, Boolean]] = column[Map[YearMonth, Boolean]]("history")
      |
      |      def * = (id, history)
      |    }
      |""".stripMargin
  }
}
