package pl.iterators.kebs.instances.time

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.core.instances.InstanceConverter
import InstanceConverter.DecodeErrorException

import java.time._

class TimeInstancesTests extends AnyFunSuite with Matchers with TimeInstances {

  test("DayOfWeek to Int") {
    val ico   = implicitly[InstanceConverter[DayOfWeek, Int]]
    val value = 1
    val obj   = DayOfWeek.of(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("DayOfWeek wrong format exception") {
    val ico   = implicitly[InstanceConverter[DayOfWeek, Int]]
    val value = 8

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("Duration to String") {
    val ico   = implicitly[InstanceConverter[Duration, String]]
    val value = "PT1H"
    val obj   = Duration.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("Duration wrong format exception") {
    val ico   = implicitly[InstanceConverter[Duration, String]]
    val value = "NotADuration"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("Instant to String") {
    val ico   = implicitly[InstanceConverter[Instant, String]]
    val value = "2007-12-03T10:15:30Z"
    val obj   = Instant.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("Instant wrong format exception") {
    val ico   = implicitly[InstanceConverter[Instant, String]]
    val value = "NotAnInstant"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("LocalDate to String") {
    val ico   = implicitly[InstanceConverter[LocalDate, String]]
    val value = "2007-12-03"
    val obj   = LocalDate.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("LocalDate wrong format exception") {
    val ico   = implicitly[InstanceConverter[LocalDate, String]]
    val value = "NotALocalDate"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("LocalDateTime to String") {
    val ico   = implicitly[InstanceConverter[LocalDateTime, String]]
    val value = "2007-12-03T10:15:30"
    val obj   = LocalDateTime.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("LocalDateTime wrong format exception") {
    val ico   = implicitly[InstanceConverter[LocalDateTime, String]]
    val value = "NotALocalDateTime"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("LocalTime to String") {
    val ico   = implicitly[InstanceConverter[LocalTime, String]]
    val value = "10:15:30"
    val obj   = LocalTime.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("LocalTime wrong format exception") {
    val ico   = implicitly[InstanceConverter[LocalTime, String]]
    val value = "NotALocalTime"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("Month to Int") {
    val ico   = implicitly[InstanceConverter[Month, Int]]
    val value = 12
    val obj   = Month.of(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("Month wrong format exception") {
    val ico   = implicitly[InstanceConverter[Month, Int]]
    val value = 13

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("MonthDay to String") {
    val ico   = implicitly[InstanceConverter[MonthDay, String]]
    val value = "--12-03"
    val obj   = MonthDay.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("MonthDay wrong format exception") {
    val ico   = implicitly[InstanceConverter[MonthDay, String]]
    val value = "NotAMonthDay"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("OffsetDateTime to String") {
    val ico   = implicitly[InstanceConverter[OffsetDateTime, String]]
    val value = "2011-12-03T10:15:30+01:00"
    val obj   = OffsetDateTime.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("OffsetDateTime wrong format exception") {
    val ico   = implicitly[InstanceConverter[OffsetDateTime, String]]
    val value = "NotAnOffsetDateTime"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("OffsetTime to String") {
    val ico   = implicitly[InstanceConverter[OffsetTime, String]]
    val value = "10:15:30+01:00"
    val obj   = OffsetTime.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("OffsetTime wrong format exception") {
    val ico   = implicitly[InstanceConverter[OffsetTime, String]]
    val value = "NotAnOffsetTime"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("Period to String") {
    val ico   = implicitly[InstanceConverter[Period, String]]
    val value = "P2Y"
    val obj   = Period.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("Period wrong format exception") {
    val ico   = implicitly[InstanceConverter[Period, String]]
    val value = "NotAPeriod"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("Year to Int") {
    val ico   = implicitly[InstanceConverter[Year, Int]]
    val value = 2007
    val obj   = Year.of(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("Year wrong format exception") {
    val ico   = implicitly[InstanceConverter[Year, Int]]
    val value = Int.MinValue

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("YearMonth to String") {
    val ico   = implicitly[InstanceConverter[YearMonth, String]]
    val value = "2011-12"
    val obj   = YearMonth.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("YearMonth wrong format exception") {
    val ico   = implicitly[InstanceConverter[YearMonth, String]]
    val value = "NotAYearMonth"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("ZoneId to String") {
    val ico   = implicitly[InstanceConverter[ZoneId, String]]
    val value = "Europe/Warsaw"
    val obj   = ZoneId.of(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("ZoneId wrong format exception") {
    val ico   = implicitly[InstanceConverter[ZoneId, String]]
    val value = "NotAZoneId"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("ZoneOffset to String") {
    val ico   = implicitly[InstanceConverter[ZoneOffset, String]]
    val value = "+01:00"
    val obj   = ZoneOffset.of(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("ZoneOffset wrong format exception") {
    val ico   = implicitly[InstanceConverter[ZoneOffset, String]]
    val value = "NotAZoneOffset"

    assertThrows[DecodeErrorException](ico.decode(value))
  }

  test("ZonedDateTime to String") {
    val ico   = implicitly[InstanceConverter[ZonedDateTime, String]]
    val value = "2011-12-03T10:15:30+01:00[Europe/Warsaw]"
    val obj   = ZonedDateTime.parse(value)

    ico.encode(obj) shouldBe value
    ico.decode(value) shouldBe obj
  }

  test("ZonedDateTime wrong format exception") {
    val ico   = implicitly[InstanceConverter[ZonedDateTime, String]]
    val value = "NotAZoneOffset"

    assertThrows[DecodeErrorException](ico.decode(value))
  }
}
