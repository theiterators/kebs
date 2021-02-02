package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.time._
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.zone.ZoneRulesException

trait TimeInstances {
  import TimeInstances._

  implicit val dayOfWeekRep: CaseClass1Rep[DayOfWeek, Int] =
    new CaseClass1Rep[DayOfWeek, Int](tryParse[DayOfWeek, Int](DayOfWeek.of, classOf[DayOfWeek], DayOfWeekFormat), _.getValue)

  implicit val durationRep: CaseClass1Rep[Duration, String] =
    new CaseClass1Rep[Duration, String](tryParse[Duration, String](Duration.parse, classOf[Duration], DurationFormat), _.toString)

  implicit val instantRep: CaseClass1Rep[Instant, String] =
    new CaseClass1Rep[Instant, String](tryParse[Instant, String](Instant.parse, classOf[Instant], InstantFormat), _.toString)

  implicit val localDateRep: CaseClass1Rep[LocalDate, String] = new CaseClass1Rep[LocalDate, String](
    tryParse[LocalDate, String](LocalDate.parse(_, localDateFormatter), classOf[LocalDate], LocalDateFormat),
    _.format(localDateFormatter))

  implicit val localDateTimeRep: CaseClass1Rep[LocalDateTime, String] = new CaseClass1Rep[LocalDateTime, String](
    tryParse[LocalDateTime, String](LocalDateTime.parse(_, localDateTimeFormatter), classOf[LocalDateTime], LocalDateTimeFormat),
    _.format(localDateTimeFormatter)
  )

  implicit val localTimeRep: CaseClass1Rep[LocalTime, String] = new CaseClass1Rep[LocalTime, String](
    tryParse[LocalTime, String](LocalTime.parse, classOf[LocalTime], LocalTimeFormat),
    _.format(localTimeFormatter))

  implicit val monthRep: CaseClass1Rep[Month, Int] =
    new CaseClass1Rep[Month, Int](tryParse[Month, Int](Month.of, classOf[Month], MonthFormat), _.getValue)

  implicit val monthDayRep: CaseClass1Rep[MonthDay, String] = new CaseClass1Rep[MonthDay, String](
    tryParse[MonthDay, String](MonthDay.parse(_, monthDayFormatter), classOf[MonthDay], MonthDayFormat),
    _.format(monthDayFormatter))

  implicit val offsetDateTimeRep: CaseClass1Rep[OffsetDateTime, String] = new CaseClass1Rep[OffsetDateTime, String](
    tryParse[OffsetDateTime, String](OffsetDateTime.parse(_, offsetDateTimeFormatter), classOf[OffsetDateTime], OffsetDateTimeFormat),
    _.format(offsetDateTimeFormatter)
  )

  implicit val offsetTimeRep: CaseClass1Rep[OffsetTime, String] = new CaseClass1Rep[OffsetTime, String](
    tryParse[OffsetTime, String](OffsetTime.parse(_, offsetTimeFormatter), classOf[OffsetTime], OffsetTimeFormat),
    _.format(offsetTimeFormatter))

  implicit val periodRep: CaseClass1Rep[Period, String] =
    new CaseClass1Rep[Period, String](tryParse[Period, String](Period.parse, classOf[Period], PeriodFormat), _.toString)

  implicit val yearRep: CaseClass1Rep[Year, String] =
    new CaseClass1Rep[Year, String](tryParse[Year, String](Year.parse, classOf[Year], YearFormat), _.toString)

  implicit val yearMonthRep: CaseClass1Rep[YearMonth, String] = new CaseClass1Rep[YearMonth, String](
    tryParse[YearMonth, String](YearMonth.parse(_, yearMonthFormatter), classOf[YearMonth], YearMonthFormat),
    _.format(yearMonthFormatter))

  implicit val zoneIdRep: CaseClass1Rep[ZoneId, String] =
    new CaseClass1Rep[ZoneId, String](tryParse[ZoneId, String](ZoneId.of, classOf[ZoneId], ZoneIdFormat), _.toString)

  implicit val zoneOffsetRep: CaseClass1Rep[ZoneOffset, String] =
    new CaseClass1Rep[ZoneOffset, String](tryParse[ZoneOffset, String](ZoneOffset.of, classOf[ZoneOffset], ZoneOffsetFormat), _.toString)

  implicit val zonedDateTimeRep: CaseClass1Rep[ZonedDateTime, String] = new CaseClass1Rep[ZonedDateTime, String](
    tryParse[ZonedDateTime, String](ZonedDateTime.parse(_, zonedDateTimeFormatter), classOf[ZonedDateTime], ZonedDateTimeFormat),
    _.format(zonedDateTimeFormatter)
  )

  private def tryParse[Obj, Val](parse: Val => Obj, clazz: Class[Obj], format: String)(value: Val): Obj =
    try {
      parse(value)
    } catch {
      case e: ZoneRulesException =>
        throw new IllegalArgumentException(FormatMsg[Obj, Val](clazz, value, format), e)
      case e: DateTimeParseException =>
        throw new IllegalArgumentException(FormatMsg[Obj, Val](clazz, value, format), e)
      case e: DateTimeException =>
        throw new IllegalArgumentException(FormatMsg[Obj, Val](clazz, value, format), e)
      case e: Throwable => throw e
    }
}

object TimeInstances {
  private[instances] val DayOfWeekFormat      = "integer in range 1 to 7"
  private[instances] val MonthFormat          = "integer in range 1 to 12"
  private[instances] val ZoneIdFormat         = "IANA standard format e.g. Europe/Warsaw"
  private[instances] val DurationFormat       = "ISO-8601 standard format e.g. PT20.345S"
  private[instances] val InstantFormat        = "ISO-8601 standard format e.g. 2007-12-03T10:15:30.00Z"
  private[instances] val LocalDateFormat      = "ISO-8601 standard format e.g. 2007-12-03"
  private[instances] val LocalDateTimeFormat  = "ISO-8601 standard format e.g. 2007-12-03T10:15:30"
  private[instances] val LocalTimeFormat      = "ISO-8601 standard format e.g. 10:15:30"
  private[instances] val MonthDayFormat       = "ISO-8601 standard format e.g. --12-03"
  private[instances] val OffsetDateTimeFormat = "ISO-8601 standard format e.g. 2011-12-03T10:15:30+01:00"
  private[instances] val OffsetTimeFormat     = "ISO-8601 standard format e.g. 10:15:30+01:00"
  private[instances] val PeriodFormat         = "ISO-8601 standard format e.g. P2Y"
  private[instances] val YearFormat           = "ISO-8601 standard format e.g. 2007"
  private[instances] val YearMonthFormat      = "ISO-8601 standard format e.g. 2011-12"
  private[instances] val ZoneOffsetFormat     = "ISO-8601 standard format e.g. +01:00"
  private[instances] val ZonedDateTimeFormat  = "ISO-8601 standard format extended with zone e.g. 2011-12-03T10:15:30+01:00[Europe/Warsaw]"

  private val monthDayFormatter       = DateTimeFormatter.ofPattern("--MM-dd")
  private val yearMonthFormatter      = DateTimeFormatter.ofPattern("uuuu-MM")
  private val localDateFormatter      = DateTimeFormatter.ISO_LOCAL_DATE
  private val localDateTimeFormatter  = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  private val localTimeFormatter      = DateTimeFormatter.ISO_LOCAL_TIME
  private val offsetDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  private val offsetTimeFormatter     = DateTimeFormatter.ISO_OFFSET_TIME
  private val zonedDateTimeFormatter  = DateTimeFormatter.ISO_ZONED_DATE_TIME

  private[instances] def FormatMsg[Obj, Val](clazz: Class[Obj], value: Val, format: String): String =
    s"${clazz.getName} cannot be parsed from $value – should be in format $format"

  trait DurationNanos extends TimeInstances {
    implicit val durationNanosRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofNanos, _.toNanos)
  }

  trait DurationMillis extends TimeInstances {
    implicit val durationMillisRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofMillis, _.toMillis)
  }

  trait DurationMinutes extends TimeInstances {
    implicit val durationMinutesRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofMinutes, _.toMinutes)
  }

  trait InstantEpochMilli extends TimeInstances {
    implicit val instantEpochMilliRep: CaseClass1Rep[Instant, Long] =
      new CaseClass1Rep[Instant, Long](Instant.ofEpochMilli, _.toEpochMilli)
  }

  trait InstantEpochSecond extends TimeInstances {
    implicit val instantEpochSecondRep: CaseClass1Rep[Instant, Long] =
      new CaseClass1Rep[Instant, Long](Instant.ofEpochSecond, _.getEpochSecond)
  }

  trait PeriodYears extends TimeInstances {
    implicit val periodYearsRep: CaseClass1Rep[Period, Int] =
      new CaseClass1Rep[Period, Int](Period.ofYears, _.getYears)
  }

  trait PeriodMonths extends TimeInstances {
    implicit val periodMonthsRep: CaseClass1Rep[Period, Int] =
      new CaseClass1Rep[Period, Int](Period.ofMonths, _.getMonths)
  }

  trait PeriodDays extends TimeInstances {
    implicit val periodDaysRep: CaseClass1Rep[Period, Int] =
      new CaseClass1Rep[Period, Int](Period.ofDays, _.getDays)
  }
}
