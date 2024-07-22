package pl.iterators.kebs.json.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.time._

class TimeInstancesTests extends AnyFunSuite with Matchers with DefaultJsonProtocol with KebsSpray with TimeInstances {

  test("No ValueClassLike implicits derived") {
    import pl.iterators.kebs.core.macros.ValueClassLike

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
    val jf    = implicitly[JsonFormat[DayOfWeek]]
    val value = 1
    val obj   = DayOfWeek.of(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("DayOfWeek wrong format exception") {
    val jf    = implicitly[JsonFormat[DayOfWeek]]
    val value = 8

    assertThrows[DecodeErrorException](jf.read(JsNumber(value)))
  }

  test("Duration standard format") {
    val jf    = implicitly[JsonFormat[Duration]]
    val value = "PT1H"
    val obj   = Duration.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Duration wrong format exception") {
    val jf    = implicitly[JsonFormat[Duration]]
    val value = "NotADuration"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("Instant standard format") {
    val jf    = implicitly[JsonFormat[Instant]]
    val value = "2007-12-03T10:15:30Z"
    val obj   = Instant.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Instant wrong format exception") {
    val jf    = implicitly[JsonFormat[Instant]]
    val value = "NotAnInstant"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("LocalDate standard format") {
    val jf    = implicitly[JsonFormat[LocalDate]]
    val value = "2007-12-03"
    val obj   = LocalDate.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalDate wrong format exception") {
    val jf    = implicitly[JsonFormat[LocalDate]]
    val value = "NotALocalDate"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("LocalDateTime standard format") {
    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "2007-12-03T10:15:30"
    val obj   = LocalDateTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalDateTime wrong format exception") {
    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "NotALocalDateTime"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("LocalTime standard format") {
    val jf    = implicitly[JsonFormat[LocalTime]]
    val value = "10:15:30"
    val obj   = LocalTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalTime wrong format exception") {
    val jf    = implicitly[JsonFormat[LocalTime]]
    val value = "NotALocalTime"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("Month standard format") {
    val jf    = implicitly[JsonFormat[Month]]
    val value = 12
    val obj   = Month.of(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("Month wrong format exception") {
    val jf    = implicitly[JsonFormat[Month]]
    val value = 13

    assertThrows[DecodeErrorException](jf.read(JsNumber(value)))
  }

  test("MonthDay standard format") {
    val jf    = implicitly[JsonFormat[MonthDay]]
    val value = "--12-03"
    val obj   = MonthDay.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("MonthDay wrong format exception") {
    val jf    = implicitly[JsonFormat[MonthDay]]
    val value = "NotAMonthDay"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("OffsetDateTime standard format") {
    val jf    = implicitly[JsonFormat[OffsetDateTime]]
    val value = "2011-12-03T10:15:30+01:00"
    val obj   = OffsetDateTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("OffsetDateTime wrong format exception") {
    val jf    = implicitly[JsonFormat[OffsetDateTime]]
    val value = "NotAnOffsetDateTime"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("OffsetTime standard format") {
    val jf    = implicitly[JsonFormat[OffsetTime]]
    val value = "10:15:30+01:00"
    val obj   = OffsetTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("OffsetTime wrong format exception") {
    val jf    = implicitly[JsonFormat[OffsetTime]]
    val value = "NotAnOffsetTime"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("Period standard format") {
    val jf    = implicitly[JsonFormat[Period]]
    val value = "P2Y"
    val obj   = Period.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Period wrong format exception") {
    val jf    = implicitly[JsonFormat[Period]]
    val value = "NotAPeriod"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("Year standard format") {
    val jf    = implicitly[JsonFormat[Year]]
    val value = 2007
    val obj   = Year.of(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("Year wrong format exception") {
    val jf    = implicitly[JsonFormat[Year]]
    val value = Int.MinValue

    assertThrows[DecodeErrorException](jf.read(JsNumber(value)))
  }

  test("YearMonth standard format") {
    val jf    = implicitly[JsonFormat[YearMonth]]
    val value = "2011-12"
    val obj   = YearMonth.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("YearMonth wrong format exception") {
    val jf    = implicitly[JsonFormat[YearMonth]]
    val value = "NotAYearMonth"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("ZoneId standard format") {
    val jf    = implicitly[JsonFormat[ZoneId]]
    val value = "Europe/Warsaw"
    val obj   = ZoneId.of(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("ZoneId wrong format exception") {
    val jf    = implicitly[JsonFormat[ZoneId]]
    val value = "NotAZoneId"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("ZoneOffset standard format") {
    val jf    = implicitly[JsonFormat[ZoneOffset]]
    val value = "+01:00"
    val obj   = ZoneOffset.of(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("ZoneOffset wrong format exception") {
    val jf    = implicitly[JsonFormat[ZoneOffset]]
    val value = "NotAZoneOffset"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

  test("ZonedDateTime standard format") {
    val jf    = implicitly[JsonFormat[ZonedDateTime]]
    val value = "2011-12-03T10:15:30+01:00[Europe/Warsaw]"
    val obj   = ZonedDateTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("ZonedDateTime wrong format exception") {
    val jf    = implicitly[JsonFormat[ZonedDateTime]]
    val value = "NotAZoneOffset"

    assertThrows[DecodeErrorException](jf.read(JsString(value)))
  }

}
