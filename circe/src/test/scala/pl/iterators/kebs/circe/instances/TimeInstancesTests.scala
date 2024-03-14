 package pl.iterators.kebs.circe.instances

import io.circe.{Decoder, Encoder, Json}
 import org.scalatest.funsuite.AnyFunSuite
 import org.scalatest.matchers.should.Matchers
 import pl.iterators.kebs.circe.KebsCirce
 import pl.iterators.kebs.core.instances.InstanceConverter.DecodeErrorException
 import pl.iterators.kebs.instances.TimeInstances

 import java.time._

 class TimeInstancesTests extends AnyFunSuite with Matchers with KebsCirce with TimeInstances {

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
     val encoder    = implicitly[Encoder[DayOfWeek]]
     val decoder    = implicitly[Decoder[DayOfWeek]]
     val value = 1
     val obj   = DayOfWeek.of(value)

     encoder(obj) shouldBe Json.fromInt(value)
     decoder(Json.fromInt(value).hcursor) shouldBe Right(obj)
   }

   test("DayOfWeek wrong format exception") {
     val decoder    = implicitly[Decoder[DayOfWeek]]
     val value = 8

     decoder(Json.fromInt(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("Duration standard format") {
     val encoder    = implicitly[Encoder[Duration]]
     val decoder    = implicitly[Decoder[Duration]]
     val value = "PT1H"
     val obj   = Duration.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("Duration wrong format exception") {
     val decoder    = implicitly[Decoder[Duration]]
     val value = "NotADuration"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("Instant standard format") {
     val encoder    = implicitly[Encoder[Instant]]
     val decoder    = implicitly[Decoder[Instant]]
     val value = "2007-12-03T10:15:30Z"
     val obj   = Instant.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("Instant wrong format exception") {
     val decoder    = implicitly[Decoder[Instant]]
     val value = "NotAnInstant"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("LocalDate standard format") {
     val encoder    = implicitly[Encoder[LocalDate]]
     val decoder    = implicitly[Decoder[LocalDate]]
     val value = "2007-12-03"
     val obj   = LocalDate.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("LocalDate wrong format exception") {
     val decoder    = implicitly[Decoder[LocalDate]]
     val value = "NotALocalDate"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("LocalDateTime standard format") {
     val encoder    = implicitly[Encoder[LocalDateTime]]
     val decoder    = implicitly[Decoder[LocalDateTime]]
     val value = "2007-12-03T10:15:30"
     val obj   = LocalDateTime.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("LocalDateTime wrong format exception") {
     val decoder    = implicitly[Decoder[LocalDateTime]]
     val value = "NotALocalDateTime"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("LocalTime standard format") {
     val encoder    = implicitly[Encoder[LocalTime]]
     val decoder    = implicitly[Decoder[LocalTime]]
     val value = "10:15:30"
     val obj   = LocalTime.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("LocalTime wrong format exception") {
     val decoder    = implicitly[Decoder[LocalTime]]
     val value = "NotALocalTime"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("Month standard format") {
     val encoder    = implicitly[Encoder[Month]]
     val decoder    = implicitly[Decoder[Month]]
     val value = 12
     val obj   = Month.of(value)

     encoder(obj) shouldBe Json.fromInt(value)
     decoder(Json.fromInt(value).hcursor) shouldBe Right(obj)
   }

   test("Month wrong format exception") {
     val decoder    = implicitly[Decoder[Month]]
     val value = 13

     decoder(Json.fromInt(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("MonthDay standard format") {
     val encoder    = implicitly[Encoder[MonthDay]]
     val decoder    = implicitly[Decoder[MonthDay]]
     val value = "--12-03"
     val obj   = MonthDay.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("MonthDay wrong format exception") {
     val decoder    = implicitly[Decoder[MonthDay]]
     val value = "NotAMonthDay"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("OffsetDateTime standard format") {
     val encoder    = implicitly[Encoder[OffsetDateTime]]
     val decoder    = implicitly[Decoder[OffsetDateTime]]
     val value = "2011-12-03T10:15:30+01:00"
     val obj   = OffsetDateTime.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("OffsetDateTime wrong format exception") {
     val decoder    = implicitly[Decoder[OffsetDateTime]]
     val value = "NotAnOffsetDateTime"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("OffsetTime standard format") {
     val encoder    = implicitly[Encoder[OffsetTime]]
     val decoder    = implicitly[Decoder[OffsetTime]]
     val value = "10:15:30+01:00"
     val obj   = OffsetTime.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("OffsetTime wrong format exception") {
     val decoder    = implicitly[Decoder[OffsetTime]]
     val value = "NotAnOffsetTime"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("Period standard format") {
     val encoder    = implicitly[Encoder[Period]]
     val decoder    = implicitly[Decoder[Period]]
     val value = "P2Y"
     val obj   = Period.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("Period wrong format exception") {
     val decoder    = implicitly[Decoder[Period]]
     val value = "NotAPeriod"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("Year standard format") {
     val encoder    = implicitly[Encoder[Year]]
     val decoder    = implicitly[Decoder[Year]]
     val value = 2007
     val obj   = Year.of(value)

     encoder(obj) shouldBe Json.fromInt(value)
     decoder(Json.fromInt(value).hcursor) shouldBe Right(obj)
   }

   test("Year wrong format exception") {
     val decoder    = implicitly[Decoder[Year]]
     val value = "NotAYear"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("YearMonth standard format") {
     val encoder    = implicitly[Encoder[YearMonth]]
     val decoder    = implicitly[Decoder[YearMonth]]
     val value = "2011-12"
     val obj   = YearMonth.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("YearMonth wrong format exception") {
     val decoder    = implicitly[Decoder[YearMonth]]
     val value = "NotAYearMonth"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("ZoneId standard format") {
     val encoder    = implicitly[Encoder[ZoneId]]
     val decoder    = implicitly[Decoder[ZoneId]]
     val value = "Europe/Warsaw"
     val obj   = ZoneId.of(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("ZoneId wrong format exception") {
     val decoder    = implicitly[Decoder[ZoneId]]
     val value = "NotAZoneId"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("ZoneOffset standard format") {
     val encoder    = implicitly[Encoder[ZoneOffset]]
     val decoder    = implicitly[Decoder[ZoneOffset]]
     val value = "+01:00"
     val obj   = ZoneOffset.of(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("ZoneOffset wrong format exception") {
     val decoder    = implicitly[Decoder[ZoneOffset]]
     val value = "NotAZoneOffset"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

   test("ZonedDateTime standard format") {
     val encoder    = implicitly[Encoder[ZonedDateTime]]
     val decoder    = implicitly[Decoder[ZonedDateTime]]
     val value = "2011-12-03T10:15:30+01:00[Europe/Warsaw]"
     val obj   = ZonedDateTime.parse(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

   test("ZonedDateTime wrong format exception") {
     val decoder    = implicitly[Decoder[ZonedDateTime]]
     val value = "NotAZoneOffset"

     decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
   }

 }
