package hstore

import com.github.tminglei.slickpg._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SlickPgHstoreColumnTypeTests extends AnyFunSuite with Matchers {
  import pl.iterators.kebs.Kebs
  import pl.iterators.kebs.instances.time.{DayOfWeekInt, YearMonthString}
  import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
  import java.time.{DayOfWeek, YearMonth, Instant}

  object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
    override val api: APIWithHStore = new APIWithHStore {}
    trait APIWithHStore extends super.API with HStoreImplicits with Kebs with YearMonthString with DayOfWeekInt with InstantEpochMilliLong
  }

  case class CategoryName(name: String)
  case class CategoryImportance(value: Int)

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

  /* CaseClass1Rep[Obj, String] */
  test("Map[Obj[String], String] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, String])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                           = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[YearMonth, String]] = column[Map[YearMonth, String]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[String, Obj[String]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[String, YearMonth])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                           = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[String, YearMonth]] = column[Map[String, YearMonth]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[String], Int] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Int])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                        = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[YearMonth, Int]] = column[Map[YearMonth, Int]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Int, Obj[String]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Int, YearMonth])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                        = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Int, YearMonth]] = column[Map[Int, YearMonth]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[String], Long] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Long])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[YearMonth, Long]] = column[Map[YearMonth, Long]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Long, Obj[String]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Long, YearMonth])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Long, YearMonth]] = column[Map[Long, YearMonth]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[String], Boolean] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[YearMonth, Boolean]] = column[Map[YearMonth, Boolean]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Boolean, Obj[String]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Boolean, YearMonth])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Boolean, YearMonth]] = column[Map[Boolean, YearMonth]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  /* CaseClass1Rep[Obj, Int] */
  test("Map[Obj[Int], String] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[DayOfWeek, String])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                           = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[DayOfWeek, String]] = column[Map[DayOfWeek, String]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[String, Obj[Int]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[String, DayOfWeek])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                           = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[String, DayOfWeek]] = column[Map[String, DayOfWeek]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[Int], Int] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[DayOfWeek, Int])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                        = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[DayOfWeek, Int]] = column[Map[DayOfWeek, Int]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Int, Obj[Int]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Int, DayOfWeek])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                        = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Int, DayOfWeek]] = column[Map[Int, DayOfWeek]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[Int], Long] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[DayOfWeek, Long])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[DayOfWeek, Long]] = column[Map[DayOfWeek, Long]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Long, Obj[Int]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Long, DayOfWeek])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Long, DayOfWeek]] = column[Map[Long, DayOfWeek]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[Int], Boolean] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[DayOfWeek, Boolean])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[DayOfWeek, Boolean]] = column[Map[DayOfWeek, Boolean]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Boolean, Obj[Int]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Boolean, DayOfWeek])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Boolean, DayOfWeek]] = column[Map[Boolean, DayOfWeek]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  /* CaseClass1Rep[Obj, Long] */
  test("Map[Obj[Long], String] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Instant, String])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                           = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Instant, String]] = column[Map[Instant, String]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[String, Obj[Long]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[String, Instant])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                           = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[String, Instant]] = column[Map[String, Instant]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[Long], Int] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Instant, Int])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                        = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Instant, Int]] = column[Map[Instant, Int]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Int, Obj[Long]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Int, Instant])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                        = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Int, Instant]] = column[Map[Int, Instant]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[Long], Long] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Instant, Long])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Instant, Long]] = column[Map[Instant, Long]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Long, Obj[Long]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Long, Instant])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Long, Instant]] = column[Map[Long, Instant]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Obj[Long], Boolean] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Instant, Boolean])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Instant, Boolean]] = column[Map[Instant, Boolean]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

  test("Map[Boolean, Obj[Long]] column type") {
    """
      |class HStoreTestTable(tag: Tag) extends Table[(Long, Map[Boolean, Instant])](tag, "HStoreTestTable") {
      |      def id: Rep[Long]                         = column[Long]("id", O.AutoInc, O.PrimaryKey)
      |      def categories: Rep[Map[Boolean, Instant]] = column[Map[Boolean, Instant]]("categories")
      |
      |      def * = (id, categories)
      |    }
      |""".stripMargin should compile
  }

}
