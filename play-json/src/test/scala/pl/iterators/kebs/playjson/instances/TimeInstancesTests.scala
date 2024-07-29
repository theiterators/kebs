package pl.iterators.kebs.playjson.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.TimeInstances
import play.api.libs.json.{Format, JsNumber, JsString, JsSuccess}

import java.time._

class TimeInstancesTests extends AnyFunSuite with Matchers with TimeInstances {
  import pl.iterators.kebs.playjson._
  test("No ValueClassLike implicits derived") {

    "implicitly[ValueClassLike[DayOfWeek, Int]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Int, DayOfWeek]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Duration, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Duration]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Instant, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Instant]]" shouldNot typeCheck
    "implicitly[ValueClassLike[LocalDate, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDate]]" shouldNot typeCheck
    "implicitly[ValueClassLike[LocalDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalDateTime]]" shouldNot typeCheck
    "implicitly[ValueClassLike[LocalTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, LocalTime]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Month, Int]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Int, Month]]" shouldNot typeCheck
    "implicitly[ValueClassLike[MonthDay, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, MonthDay]]" shouldNot typeCheck
    "implicitly[ValueClassLike[OffsetDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, OffsetDateTime]]" shouldNot typeCheck
    "implicitly[ValueClassLike[OffsetTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, OffsetTime]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Period, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Period]]" shouldNot typeCheck
    "implicitly[ValueClassLike[Year, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, Year]]" shouldNot typeCheck
    "implicitly[ValueClassLike[YearMonth, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, YearMonth]]" shouldNot typeCheck
    "implicitly[ValueClassLike[ZoneId, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, ZoneId]]" shouldNot typeCheck
    "implicitly[ValueClassLike[ZoneOffset, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, ZoneOffset]]" shouldNot typeCheck
    "implicitly[ValueClassLike[ZonedDateTime, String]]" shouldNot typeCheck
    "implicitly[ValueClassLike[String, ZonedDateTime]]" shouldNot typeCheck
  }

  test("DayOfWeek standard format") {
    val jf    = implicitly[Format[DayOfWeek]]
    val value = 1
    val obj   = DayOfWeek.of(value)

    jf.writes(obj) shouldBe JsNumber(value)
    jf.reads(JsNumber(value)) shouldBe JsSuccess(obj)
  }

  test("DayOfWeek wrong format exception") {
    val jf    = implicitly[Format[DayOfWeek]]
    val value = 8

    assertThrows[DecodeErrorException](jf.reads(JsNumber(value)))
  }

  test("Duration standard format") {
    val jf    = implicitly[Format[Duration]]
    val value = "PT1H"
    val obj   = Duration.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("Duration wrong format exception") {
    val jf    = implicitly[Format[Duration]]
    val value = "NotADuration"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("Instant standard format") {
    val jf    = implicitly[Format[Instant]]
    val value = "2007-12-03T10:15:30Z"
    val obj   = Instant.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("Instant wrong format exception") {
    val jf    = implicitly[Format[Instant]]
    val value = "NotAnInstant"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("LocalDate standard format") {
    val jf    = implicitly[Format[LocalDate]]
    val value = "2007-12-03"
    val obj   = LocalDate.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("LocalDate wrong format exception") {
    val jf    = implicitly[Format[LocalDate]]
    val value = "NotALocalDate"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("LocalDateTime standard format") {
    val jf    = implicitly[Format[LocalDateTime]]
    val value = "2007-12-03T10:15:30"
    val obj   = LocalDateTime.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("LocalDateTime wrong format exception") {
    val jf    = implicitly[Format[LocalDateTime]]
    val value = "NotALocalDateTime"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("LocalTime standard format") {
    val jf    = implicitly[Format[LocalTime]]
    val value = "10:15:30"
    val obj   = LocalTime.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("LocalTime wrong format exception") {
    val jf    = implicitly[Format[LocalTime]]
    val value = "NotALocalTime"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("Month standard format") {
    val jf    = implicitly[Format[Month]]
    val value = 12
    val obj   = Month.of(value)

    jf.writes(obj) shouldBe JsNumber(value)
    jf.reads(JsNumber(value)) shouldBe JsSuccess(obj)
  }

  test("Month wrong format exception") {
    val jf    = implicitly[Format[Month]]
    val value = 13

    assertThrows[DecodeErrorException](jf.reads(JsNumber(value)))
  }

  test("MonthDay standard format") {
    val jf    = implicitly[Format[MonthDay]]
    val value = "--12-03"
    val obj   = MonthDay.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("MonthDay wrong format exception") {
    val jf    = implicitly[Format[MonthDay]]
    val value = "NotAMonthDay"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("OffsetDateTime standard format") {
    val jf    = implicitly[Format[OffsetDateTime]]
    val value = "2011-12-03T10:15:30+01:00"
    val obj   = OffsetDateTime.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("OffsetDateTime wrong format exception") {
    val jf    = implicitly[Format[OffsetDateTime]]
    val value = "NotAnOffsetDateTime"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("OffsetTime standard format") {
    val jf    = implicitly[Format[OffsetTime]]
    val value = "10:15:30+01:00"
    val obj   = OffsetTime.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("OffsetTime wrong format exception") {
    val jf    = implicitly[Format[OffsetTime]]
    val value = "NotAnOffsetTime"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("Period standard format") {
    val jf    = implicitly[Format[Period]]
    val value = "P2Y"
    val obj   = Period.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("Period wrong format exception") {
    val jf    = implicitly[Format[Period]]
    val value = "NotAPeriod"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("Year standard format") {
    val jf    = implicitly[Format[Year]]
    val value = 2007
    val obj   = Year.of(value)

    jf.writes(obj) shouldBe JsNumber(value)
    jf.reads(JsNumber(value)) shouldBe JsSuccess(obj)
  }

  test("Year wrong format exception") {
    val jf    = implicitly[Format[Year]]
    val value = Int.MinValue

    assertThrows[DecodeErrorException](jf.reads(JsNumber(value)))
  }

  test("YearMonth standard format") {
    val jf    = implicitly[Format[YearMonth]]
    val value = "2011-12"
    val obj   = YearMonth.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("YearMonth wrong format exception") {
    val jf    = implicitly[Format[YearMonth]]
    val value = "NotAYearMonth"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("ZoneId standard format") {
    val jf    = implicitly[Format[ZoneId]]
    val value = "Europe/Warsaw"
    val obj   = ZoneId.of(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("ZoneId wrong format exception") {
    val jf    = implicitly[Format[ZoneId]]
    val value = "NotAZoneId"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("ZoneOffset standard format") {
    val jf    = implicitly[Format[ZoneOffset]]
    val value = "+01:00"
    val obj   = ZoneOffset.of(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("ZoneOffset wrong format exception") {
    val jf    = implicitly[Format[ZoneOffset]]
    val value = "NotAZoneOffset"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

  test("ZonedDateTime standard format") {
    val jf    = implicitly[Format[ZonedDateTime]]
    val value = "2011-12-03T10:15:30+01:00[Europe/Warsaw]"
    val obj   = ZonedDateTime.parse(value)

    jf.writes(obj) shouldBe JsString(value)
    jf.reads(JsString(value)) shouldBe JsSuccess(obj)
  }

  test("ZonedDateTime wrong format exception") {
    val jf    = implicitly[Format[ZonedDateTime]]
    val value = "NotAZoneOffset"

    assertThrows[DecodeErrorException](jf.reads(JsString(value)))
  }

}
