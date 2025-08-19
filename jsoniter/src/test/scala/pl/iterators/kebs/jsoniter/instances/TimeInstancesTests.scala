package pl.iterators.kebs.jsoniter.instances

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.instances.TimeInstances

import java.time._

class TimeInstancesTests extends AnyFunSuite with Matchers with KebsJsoniter with TimeInstances {
  private def isScalaJS     = System.getProperty("java.vm.name") == "Scala.js"
  private def isScalaNative = System.getProperty("java.vm.name") == "Scala Native"

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
    val codec = implicitly[JsonValueCodec[DayOfWeek]]
    val value = 1
    val obj   = DayOfWeek.of(value)

    writeToString[DayOfWeek](obj)(codec) shouldBe value.toString
    readFromString[DayOfWeek](value.toString)(codec) shouldBe obj
  }

  test("DayOfWeek wrong format exception") {
    val codec = implicitly[JsonValueCodec[DayOfWeek]]
    val value = 8

    an[Exception] should be thrownBy readFromString[DayOfWeek](value.toString)(codec)
  }

  test("Duration standard format") {
    val codec = implicitly[JsonValueCodec[Duration]]
    val value = "PT1H"
    val obj   = Duration.parse(value)

    writeToString[Duration](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[Duration]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("Duration wrong format exception") {
    val codec = implicitly[JsonValueCodec[Duration]]
    val value = "NotADuration"

    an[Exception] should be thrownBy readFromString[Duration]("\"" + value + "\"")(codec)
  }

  test("Instant standard format") {
    val codec = implicitly[JsonValueCodec[Instant]]
    val value = "2007-12-03T10:15:30Z"
    val obj   = Instant.parse(value)

    writeToString[Instant](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[Instant]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("Instant wrong format exception") {
    val codec = implicitly[JsonValueCodec[Instant]]
    val value = "NotAnInstant"

    an[Exception] should be thrownBy readFromString[Instant]("\"" + value + "\"")(codec)
  }

  test("LocalDate standard format") {
    val codec = implicitly[JsonValueCodec[LocalDate]]
    val value = "2007-12-03"
    val obj   = LocalDate.parse(value)

    writeToString[LocalDate](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[LocalDate]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("LocalDate wrong format exception") {
    val codec = implicitly[JsonValueCodec[LocalDate]]
    val value = "NotALocalDate"

    an[Exception] should be thrownBy readFromString[LocalDate]("\"" + value + "\"")(codec)
  }

  test("LocalDateTime standard format") {
    val codec = implicitly[JsonValueCodec[LocalDateTime]]
    val value = "2007-12-03T10:15:30"
    val obj   = LocalDateTime.parse(value)

    writeToString[LocalDateTime](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[LocalDateTime]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("LocalDateTime wrong format exception") {
    val codec = implicitly[JsonValueCodec[LocalDateTime]]
    val value = "NotALocalDateTime"

    an[Exception] should be thrownBy readFromString[LocalDateTime]("\"" + value + "\"")(codec)
  }

  test("LocalTime standard format") {
    val codec = implicitly[JsonValueCodec[LocalTime]]
    val value = "10:15:30"
    val obj   = LocalTime.parse(value)

    writeToString[LocalTime](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[LocalTime]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("LocalTime wrong format exception") {
    val codec = implicitly[JsonValueCodec[LocalTime]]
    val value = "NotALocalTime"

    an[Exception] should be thrownBy readFromString[LocalTime]("\"" + value + "\"")(codec)
  }

  test("Month standard format") {
    val codec = implicitly[JsonValueCodec[Month]]
    val value = 12
    val obj   = Month.of(value)

    writeToString[Month](obj)(codec) shouldBe value.toString
    readFromString[Month](value.toString)(codec) shouldBe obj
  }

  test("Month wrong format exception") {
    val codec = implicitly[JsonValueCodec[Month]]
    val value = 13

    an[Exception] should be thrownBy readFromString[Month](value.toString)(codec)
  }

  test("MonthDay standard format") {
    val codec = implicitly[JsonValueCodec[MonthDay]]
    val value = "--12-03"
    val obj   = MonthDay.parse(value)

    writeToString[MonthDay](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[MonthDay]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("MonthDay wrong format exception") {
    val codec = implicitly[JsonValueCodec[MonthDay]]
    val value = "NotAMonthDay"

    an[Exception] should be thrownBy readFromString[MonthDay]("\"" + value + "\"")(codec)
  }

  test("OffsetDateTime standard format") {
    val codec = implicitly[JsonValueCodec[OffsetDateTime]]
    val value = "2011-12-03T10:15:30+01:00"
    val obj   = OffsetDateTime.parse(value)

    writeToString[OffsetDateTime](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[OffsetDateTime]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("OffsetDateTime wrong format exception") {
    val codec = implicitly[JsonValueCodec[OffsetDateTime]]
    val value = "NotAnOffsetDateTime"

    an[Exception] should be thrownBy readFromString[OffsetDateTime]("\"" + value + "\"")(codec)
  }

  test("OffsetTime standard format") {
    val codec = implicitly[JsonValueCodec[OffsetTime]]
    val value = "10:15:30+01:00"
    val obj   = OffsetTime.parse(value)

    writeToString[OffsetTime](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[OffsetTime]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("OffsetTime wrong format exception") {
    val codec = implicitly[JsonValueCodec[OffsetTime]]
    val value = "NotAnOffsetTime"

    an[Exception] should be thrownBy readFromString[OffsetTime]("\"" + value + "\"")(codec)
  }

  test("Period standard format") {
    val codec = implicitly[JsonValueCodec[Period]]
    val value = "P2Y"
    val obj   = Period.parse(value)

    writeToString[Period](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[Period]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("Period wrong format exception") {
    val codec = implicitly[JsonValueCodec[Period]]
    val value = "NotAPeriod"

    an[Exception] should be thrownBy readFromString[Period]("\"" + value + "\"")(codec)
  }

  test("Year standard format") {
    val codec = implicitly[JsonValueCodec[Year]]
    val value = 2007
    val obj   = Year.of(value)

    writeToString[Year](obj)(codec) shouldBe value.toString
    readFromString[Year](value.toString)(codec) shouldBe obj
  }

  test("Year wrong format exception") {
    val codec = implicitly[JsonValueCodec[Year]]
    val value = "NotAYear"

    an[JsonReaderException] should be thrownBy readFromString[Year]("\"" + value + "\"")(codec)
  }

  test("YearMonth standard format") {
    val codec = implicitly[JsonValueCodec[YearMonth]]
    val value = "2011-12"
    val obj   = YearMonth.parse(value)

    writeToString[YearMonth](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[YearMonth]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("YearMonth wrong format exception") {
    val codec = implicitly[JsonValueCodec[YearMonth]]
    val value = "NotAYearMonth"

    an[Exception] should be thrownBy readFromString[YearMonth]("\"" + value + "\"")(codec)
  }

  test("ZoneId standard format") {
    if (!isScalaJS && !isScalaNative) {
      val codec = implicitly[JsonValueCodec[ZoneId]]
      val value = "Europe/Warsaw"
      val obj   = ZoneId.of(value)

      writeToString[ZoneId](obj)(codec) shouldBe "\"" + value + "\""
      readFromString[ZoneId]("\"" + value + "\"")(codec) shouldBe obj
    }
  }

  test("ZoneId wrong format exception") {
    val codec = implicitly[JsonValueCodec[ZoneId]]
    val value = "NotAZoneId"

    an[Exception] should be thrownBy readFromString[ZoneId]("\"" + value + "\"")(codec)
  }

  test("ZoneOffset standard format") {
    val codec = implicitly[JsonValueCodec[ZoneOffset]]
    val value = "+01:00"
    val obj   = ZoneOffset.of(value)

    writeToString[ZoneOffset](obj)(codec) shouldBe "\"" + value + "\""
    readFromString[ZoneOffset]("\"" + value + "\"")(codec) shouldBe obj
  }

  test("ZoneOffset wrong format exception") {
    val codec = implicitly[JsonValueCodec[ZoneOffset]]
    val value = "NotAZoneOffset"

    an[Exception] should be thrownBy readFromString[ZoneOffset]("\"" + value + "\"")(codec)
  }

  test("ZonedDateTime standard format") {
    if (!isScalaJS && !isScalaNative) {
      val codec = implicitly[JsonValueCodec[ZonedDateTime]]
      val value = "2011-12-03T10:15:30+01:00[Europe/Warsaw]"
      val obj   = ZonedDateTime.parse(value)

      writeToString[ZonedDateTime](obj)(codec) shouldBe "\"" + value + "\""
      readFromString[ZonedDateTime]("\"" + value + "\"")(codec) shouldBe obj
    }
  }

  test("ZonedDateTime wrong format exception") {
    val codec = implicitly[JsonValueCodec[ZonedDateTime]]
    val value = "NotAZoneOffset"

    an[Exception] should be thrownBy readFromString[ZonedDateTime]("\"" + value + "\"")(codec)
  }
}
