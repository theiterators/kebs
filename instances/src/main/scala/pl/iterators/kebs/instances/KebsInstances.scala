package pl.iterators.kebs.instances

import java.net.{URI, URL}
import java.time.{
  DayOfWeek,
  Duration,
  Instant,
  LocalDate,
  LocalDateTime,
  LocalTime,
  Month,
  MonthDay,
  OffsetDateTime,
  OffsetTime,
  Period,
  Year,
  YearMonth,
  ZoneId,
  ZoneOffset,
  ZonedDateTime
}
import java.util.{Currency, Locale, UUID}

import pl.iterators.kebs.macros.CaseClass1Rep

trait KebsInstances extends DateTimeFormat {
  implicit val uriClass1Rep: CaseClass1Rep[URI, String] =
    new CaseClass1Rep[URI, String](new URI(_), _.toString)

  implicit val urlCaseClass1Rep: CaseClass1Rep[URL, String] =
    new CaseClass1Rep[URL, String](new URL(_), _.toString)

  implicit val dayOfWeekClass1Rep: CaseClass1Rep[DayOfWeek, Int] =
    new CaseClass1Rep[DayOfWeek, Int](DayOfWeek.of, _.getValue)

  implicit val durationClass1Rep: CaseClass1Rep[Duration, String] =
    new CaseClass1Rep[Duration, String](Duration.parse(_), _.toString)

  implicit val instantClass1Rep: CaseClass1Rep[Instant, String] =
    new CaseClass1Rep[Instant, String](Instant.parse(_), _.toString)

  implicit val localDateCaseClass1Rep: CaseClass1Rep[LocalDate, String] =
    new CaseClass1Rep[LocalDate, String](LocalDate.parse(_, localDateFormatter), _.format(localDateFormatter))

  implicit val localDateTimeCaseClass1Rep: CaseClass1Rep[LocalDateTime, String] =
    new CaseClass1Rep[LocalDateTime, String](LocalDateTime.parse(_, localDateTimeFormatter), _.format(localDateTimeFormatter))

  implicit val localTimeCaseClass1Rep: CaseClass1Rep[LocalTime, String] =
    new CaseClass1Rep[LocalTime, String](LocalTime.parse(_, localTimeFormatter), _.format(localTimeFormatter))

  implicit val monthClass1Rep: CaseClass1Rep[Month, Int] =
    new CaseClass1Rep[Month, Int](Month.of, _.getValue)

  // --MM-dd
  implicit val monthDayClass1Rep: CaseClass1Rep[MonthDay, String] =
    new CaseClass1Rep[MonthDay, String](MonthDay.parse(_), _.toString)

  implicit val offsetDateTimeClass1Rep: CaseClass1Rep[OffsetDateTime, String] =
    new CaseClass1Rep[OffsetDateTime, String](OffsetDateTime.parse(_, offsetDateTimeFormatter), _.format(offsetDateTimeFormatter))

  implicit val offsetTimeClass1Rep: CaseClass1Rep[OffsetTime, String] =
    new CaseClass1Rep[OffsetTime, String](OffsetTime.parse(_, offsetTimeFormatter), _.format(offsetTimeFormatter))

  implicit val periodClass1Rep: CaseClass1Rep[Period, String] =
    new CaseClass1Rep[Period, String](Period.parse(_), _.toString)

  implicit val yearClass1Rep: CaseClass1Rep[Year, String] =
    new CaseClass1Rep[Year, String](Year.parse(_), _.toString)

  // uuuu-MM
  implicit val yearMonthClass1Rep: CaseClass1Rep[YearMonth, String] =
    new CaseClass1Rep[YearMonth, String](YearMonth.parse(_), _.toString)

  implicit val zoneIdClass1Rep: CaseClass1Rep[ZoneId, String] =
    new CaseClass1Rep[ZoneId, String](ZoneId.of, _.toString)

  implicit val zoneOffsetClass1Rep: CaseClass1Rep[ZoneOffset, String] =
    new CaseClass1Rep[ZoneOffset, String](ZoneOffset.of, _.toString)

  implicit val zonedDateTimeClass1Rep: CaseClass1Rep[ZonedDateTime, String] =
    new CaseClass1Rep[ZonedDateTime, String](ZonedDateTime.parse(_, zonedDateTimeFormatter), _.format(zonedDateTimeFormatter))

  implicit val currencyClass1Rep: CaseClass1Rep[Currency, String] =
    new CaseClass1Rep[Currency, String](Currency.getInstance, _.toString)

  implicit val localeClass1Rep: CaseClass1Rep[Locale, String] =
    new CaseClass1Rep[Locale, String](Locale.forLanguageTag, _.toLanguageTag)

  implicit val uuidCaseClass1Rep: CaseClass1Rep[UUID, String] =
    new CaseClass1Rep[UUID, String](UUID.fromString, _.toString)

}
