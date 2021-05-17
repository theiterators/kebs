package pl.iterators.kebs.instances

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.instances.TimeInstances._
import pl.iterators.kebs.json.KebsSpray
import spray.json._

import java.time._
class TimeInstancesTests
    extends AnyFunSuite
    with Matchers
    with DefaultJsonProtocol
    with KebsSpray
    with TimeInstances
    with DurationString
    with InstantString
    with DayOfWeekInt
    with LocalDateTimeString
    with LocalDateString
    with LocalTimeString
    with YearMonthString
    with MonthInt
    with MonthDayString
    with PeriodString
    with OffsetDateTimeString
    with OffsetTimeString
    with YearString
    with ZoneIdString
    with ZoneOffsetString
    with ZonedDateTimeString {

  test("DayOfWeek standard format") {
    val jf    = implicitly[JsonFormat[DayOfWeek]]
    val value = 1
    val obj   = DayOfWeek.of(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("DayOfWeek wrong format exception") {
    import TimeInstances.DayOfWeekFormat

    val jf    = implicitly[JsonFormat[DayOfWeek]]
    val value = 8

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsNumber(value))
    }
    assert(thrown.getMessage === errorMessage[DayOfWeek, Int](classOf[DayOfWeek], value, DayOfWeekFormat))
  }

  test("Duration standard format") {
    val jf    = implicitly[JsonFormat[Duration]]
    val value = "PT1H"
    val obj   = Duration.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Duration wrong format exception") {
    import TimeInstances.DurationFormat

    val jf    = implicitly[JsonFormat[Duration]]
    val value = "NotADuration"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[Duration, String](classOf[Duration], value, DurationFormat))
  }

  test("Instant standard format") {
    val jf    = implicitly[JsonFormat[Instant]]
    val value = "2007-12-03T10:15:30Z"
    val obj   = Instant.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Instant wrong format exception") {
    import TimeInstances.InstantFormat

    val jf = implicitly[JsonFormat[Instant]]

    val value = "NotAnInstant"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[Instant, String](classOf[Instant], value, InstantFormat))
  }

  test("LocalDate standard format") {
    val jf    = implicitly[JsonFormat[LocalDate]]
    val value = "2007-12-03"
    val obj   = LocalDate.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalDate wrong format exception") {
    import TimeInstances.LocalDateFormat

    val jf    = implicitly[JsonFormat[LocalDate]]
    val value = "NotALocalDate"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[LocalDate, String](classOf[LocalDate], value, LocalDateFormat))
  }

  test("LocalDateTime standard format") {
    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "2007-12-03T10:15:30"
    val obj   = LocalDateTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalDateTime wrong format exception") {
    import TimeInstances.LocalDateTimeFormat

    val jf    = implicitly[JsonFormat[LocalDateTime]]
    val value = "NotALocalDateTime"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[LocalDateTime, String](classOf[LocalDateTime], value, LocalDateTimeFormat))
  }

  test("LocalTime standard format") {
    val jf    = implicitly[JsonFormat[LocalTime]]
    val value = "10:15:30"
    val obj   = LocalTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("LocalTime wrong format exception") {
    import TimeInstances.LocalTimeFormat

    val jf    = implicitly[JsonFormat[LocalTime]]
    val value = "NotALocalTime"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[LocalTime, String](classOf[LocalTime], value, LocalTimeFormat))
  }

  test("Month standard format") {
    val jf    = implicitly[JsonFormat[Month]]
    val value = 12
    val obj   = Month.of(value)

    jf.write(obj) shouldBe JsNumber(value)
    jf.read(JsNumber(value)) shouldBe obj
  }

  test("Month wrong format exception") {
    import TimeInstances.MonthFormat

    val jf    = implicitly[JsonFormat[Month]]
    val value = 13

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsNumber(value))
    }
    assert(thrown.getMessage === errorMessage[Month, Int](classOf[Month], value, MonthFormat))
  }

  test("MonthDay standard format") {
    val jf    = implicitly[JsonFormat[MonthDay]]
    val value = "--12-03"
    val obj   = MonthDay.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("MonthDay wrong format exception") {
    import TimeInstances.MonthDayFormat

    val jf    = implicitly[JsonFormat[MonthDay]]
    val value = "NotAMonthDay"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[MonthDay, String](classOf[MonthDay], value, MonthDayFormat))
  }

  test("OffsetDateTime standard format") {
    val jf    = implicitly[JsonFormat[OffsetDateTime]]
    val value = "2011-12-03T10:15:30+01:00"
    val obj   = OffsetDateTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("OffsetDateTime wrong format exception") {
    import TimeInstances.OffsetDateTimeFormat

    val jf    = implicitly[JsonFormat[OffsetDateTime]]
    val value = "NotAnOffsetDateTime"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[OffsetDateTime, String](classOf[OffsetDateTime], value, OffsetDateTimeFormat))
  }

  test("OffsetTime standard format") {
    val jf    = implicitly[JsonFormat[OffsetTime]]
    val value = "10:15:30+01:00"
    val obj   = OffsetTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("OffsetTime wrong format exception") {
    import TimeInstances.OffsetTimeFormat

    val jf    = implicitly[JsonFormat[OffsetTime]]
    val value = "NotAnOffsetTime"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[OffsetTime, String](classOf[OffsetTime], value, OffsetTimeFormat))
  }

  test("Period standard format") {
    val jf    = implicitly[JsonFormat[Period]]
    val value = "P2Y"
    val obj   = Period.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Period wrong format exception") {
    import TimeInstances.PeriodFormat

    val jf    = implicitly[JsonFormat[Period]]
    val value = "NotAPeriod"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[Period, String](classOf[Period], value, PeriodFormat))
  }

  test("Year standard format") {
    val jf    = implicitly[JsonFormat[Year]]
    val value = "2007"
    val obj   = Year.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("Year wrong format exception") {
    import TimeInstances.YearFormat

    val jf    = implicitly[JsonFormat[Year]]
    val value = "NotAYear"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[Year, String](classOf[Year], value, YearFormat))
  }

  test("YearMonth standard format") {
    val jf    = implicitly[JsonFormat[YearMonth]]
    val value = "2011-12"
    val obj   = YearMonth.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("YearMonth wrong format exception") {
    import TimeInstances.YearMonthFormat

    val jf    = implicitly[JsonFormat[YearMonth]]
    val value = "NotAYearMonth"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[YearMonth, String](classOf[YearMonth], value, YearMonthFormat))
  }

  test("ZoneId standard format") {
    val jf    = implicitly[JsonFormat[ZoneId]]
    val value = "Europe/Warsaw"
    val obj   = ZoneId.of(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("ZoneId wrong format exception") {
    import TimeInstances.ZoneIdFormat

    val jf    = implicitly[JsonFormat[ZoneId]]
    val value = "NotAZoneId"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[ZoneId, String](classOf[ZoneId], value, ZoneIdFormat))
  }

  test("ZoneOffset standard format") {
    val jf    = implicitly[JsonFormat[ZoneOffset]]
    val value = "+01:00"
    val obj   = ZoneOffset.of(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("ZoneOffset wrong format exception") {
    import TimeInstances.ZoneOffsetFormat

    val jf    = implicitly[JsonFormat[ZoneOffset]]
    val value = "NotAZoneOffset"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[ZoneOffset, String](classOf[ZoneOffset], value, ZoneOffsetFormat))
  }

  test("ZonedDateTime standard format") {
    val jf    = implicitly[JsonFormat[ZonedDateTime]]
    val value = "2011-12-03T10:15:30+01:00[Europe/Warsaw]"
    val obj   = ZonedDateTime.parse(value)

    jf.write(obj) shouldBe JsString(value)
    jf.read(JsString(value)) shouldBe obj
  }

  test("ZonedDateTime wrong format exception") {
    import TimeInstances.ZonedDateTimeFormat

    val jf    = implicitly[JsonFormat[ZonedDateTime]]
    val value = "NotAZoneOffset"

    val thrown = intercept[IllegalArgumentException] {
      jf.read(JsString(value))
    }
    assert(thrown.getMessage === errorMessage[ZonedDateTime, String](classOf[ZonedDateTime], value, ZonedDateTimeFormat))
  }

}
