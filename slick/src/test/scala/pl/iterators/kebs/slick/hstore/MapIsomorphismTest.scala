package pl.iterators.kebs.slick.hstore

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.instances.time.{DayOfWeekInt, YearMonthString}
import pl.iterators.kebs.slick.Kebs

class MapIsomorphismTest extends AnyFunSuite with Matchers with YearMonthString with DayOfWeekInt with InstantEpochMilliLong with Kebs {
  import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
  import slick.jdbc.PostgresProfile.api._

  case class StringValue(value: String)
  case class IntValue(value: Int)

  test("No ValueClassLike implicits derived") {
    "implicitly[ValueClassLike[YearMonth, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, YearMonth]]" shouldNot typeCheck
    "implicitly[ValueClassLike[DayOfWeek, Int]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Int, DayOfWeek]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Instant, Long]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Long, Instant]]" shouldNot typeCheck
  }

  test("Case classes isomorphisms implies string to int map isomorphism") {
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[StringValue, IntValue], Map[String, Int]]]
    iso.map(Map(StringValue("a") -> IntValue(0), StringValue("b") -> IntValue(1))) shouldBe Map("a"    -> 0, "b"                        -> 1)
    iso.comap(Map("a"            -> 0, "b"                        -> 1)) shouldBe Map(StringValue("a") -> IntValue(0), StringValue("b") -> IntValue(1))
  }

    test("Case classes isomorphisms implies string to string map isomorphism") {
      val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[StringValue, IntValue], Map[String, String]]]
      iso.map(Map(StringValue("a") -> IntValue(0), StringValue("b") -> IntValue(1))) shouldBe Map("a"      -> "0", "b"                      -> "1")
      iso.comap(Map("a"            -> "0", "b"                      -> "1")) shouldBe Map(StringValue("a") -> IntValue(0), StringValue("b") -> IntValue(1))
    }

  test("Case classes isomorphisms implies int to string map isomorphism") {
    val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[IntValue, StringValue], Map[Int, String]]]
    iso.map(Map(IntValue(0) -> StringValue("a"), IntValue(1) -> StringValue("b"))) shouldBe Map(0 -> "a", 1                        -> "b")
    iso.comap(Map(0         -> "a", 1                        -> "b")) shouldBe Map(IntValue(0)    -> StringValue("a"), IntValue(1) -> StringValue("b"))
  }

    test("Case classes isomorphisms implies string to string map isomorphism 2") {
      val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[IntValue, StringValue], Map[String, String]]]
      iso.map(Map(IntValue(0) -> StringValue("a"), IntValue(1) -> StringValue("b"))) shouldBe Map("0" -> "a", "1"                      -> "b")
      iso.comap(Map("0"       -> "a", "1"                      -> "b")) shouldBe Map(IntValue(0)      -> StringValue("a"), IntValue(1) -> StringValue("b"))
    }

  import java.time.{DayOfWeek, YearMonth, Instant}

  /* InstanceConverter[Obj, String] */
  test("Map[Obj[String], String] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[YearMonth, String], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[String, Obj[String]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[String, YearMonth], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[String], Int] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[YearMonth, Int], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Int, Obj[String]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Int, YearMonth], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[String], Long] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[YearMonth, Long], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Long, Obj[String]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Long, YearMonth], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[String], Boolean] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[YearMonth, Boolean], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Boolean, Obj[String]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Boolean, YearMonth], Map[String, String]]]""".stripMargin should compile
  }

  /* implicit InstanceConverter[Obj, Int] */
  test("Map[Obj[Int], String] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[DayOfWeek, String], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[String, Obj[Int]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[String, DayOfWeek], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[Int], Int] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[DayOfWeek, Int], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Int, Obj[Int]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Int, DayOfWeek], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[Int], Long] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[DayOfWeek, Long], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Long, Obj[Int]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Long, DayOfWeek], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[Int], Boolean] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[DayOfWeek, Boolean], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Boolean, Obj[Int]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Boolean, DayOfWeek], Map[String, String]]]""".stripMargin should compile
  }

  /* implicit InstanceConverter[Obj, Long] */
  test("Map[Obj[Long], String] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Instant, String], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[String, Obj[Long]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[String, Instant], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[Long], Int] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Instant, Int], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Int, Obj[Long]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Int, Instant], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[Long], Long] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Instant, Long], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Long, Obj[Long]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Long, Instant], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Obj[Long], Boolean] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Instant, Boolean], Map[String, String]]]""".stripMargin should compile
  }

  test("Map[Boolean, Obj[Long]] <-> Map[String, String]") {
    """val iso = implicitly[slick.jdbc.JdbcTypesComponent#MappedJdbcType[Map[Boolean, Instant], Map[String, String]]]""".stripMargin should compile
  }
}
