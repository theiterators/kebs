package pl.iterators.kebs.instances

import pl.iterators.kebs.instances.TimeInstances.DecodeError
import pl.iterators.kebs.macros.CaseClass1Rep

import java.time._
import java.time.format.DateTimeFormatter

trait TimeInstances {
  import TimeInstances.Formatter

  implicit def dayOfWeekRep[T](implicit f: Formatter[DayOfWeek, T]): CaseClass1Rep[DayOfWeek, T] =
    new CaseClass1Rep[DayOfWeek, T](decodeObject[DayOfWeek, T](f.decode), f.encode)

  implicit def durationRep[T](implicit f: Formatter[Duration, T]): CaseClass1Rep[Duration, T] =
    new CaseClass1Rep[Duration, T](decodeObject[Duration, T](f.decode), f.encode)

  implicit def instantRep[T](implicit f: Formatter[Instant, T]): CaseClass1Rep[Instant, T] =
    new CaseClass1Rep[Instant, T](decodeObject[Instant, T](f.decode), f.encode)

  implicit def localDateRep[T](implicit f: Formatter[LocalDate, T]): CaseClass1Rep[LocalDate, T] =
    new CaseClass1Rep[LocalDate, T](decodeObject[LocalDate, T](f.decode), f.encode)

  implicit def localDateTimeRep[T](implicit f: Formatter[LocalDateTime, T]): CaseClass1Rep[LocalDateTime, T] =
    new CaseClass1Rep[LocalDateTime, T](decodeObject[LocalDateTime, T](f.decode), f.encode)

  implicit def localTimeRep[T](implicit f: Formatter[LocalTime, T]): CaseClass1Rep[LocalTime, T] =
    new CaseClass1Rep[LocalTime, T](decodeObject[LocalTime, T](f.decode), f.encode)

  implicit def monthRep[T](implicit f: Formatter[Month, T]): CaseClass1Rep[Month, T] =
    new CaseClass1Rep[Month, T](decodeObject[Month, T](f.decode), f.encode)

  implicit def monthDayRep[T](implicit f: Formatter[MonthDay, T]): CaseClass1Rep[MonthDay, T] =
    new CaseClass1Rep[MonthDay, T](decodeObject[MonthDay, T](f.decode), f.encode)

  implicit def offsetDateTimeRep[T](implicit f: Formatter[OffsetDateTime, T]): CaseClass1Rep[OffsetDateTime, T] =
    new CaseClass1Rep[OffsetDateTime, T](decodeObject[OffsetDateTime, T](f.decode), f.encode)

  implicit def offsetTimeRep[T](implicit f: Formatter[OffsetTime, T]): CaseClass1Rep[OffsetTime, T] =
    new CaseClass1Rep[OffsetTime, T](decodeObject[OffsetTime, T](f.decode), f.encode)

  implicit def periodRep[T](implicit f: Formatter[Period, T]): CaseClass1Rep[Period, T] =
    new CaseClass1Rep[Period, T](decodeObject[Period, T](f.decode), f.encode)

  implicit def yearRep[T](implicit f: Formatter[Year, T]): CaseClass1Rep[Year, T] =
    new CaseClass1Rep[Year, T](decodeObject[Year, T](f.decode), f.encode)

  implicit def yearMonthRep[T](implicit f: Formatter[YearMonth, T]): CaseClass1Rep[YearMonth, T] =
    new CaseClass1Rep[YearMonth, T](decodeObject[YearMonth, T](f.decode), f.encode)

  implicit def zoneIdRep[T](implicit f: Formatter[ZoneId, T]): CaseClass1Rep[ZoneId, T] =
    new CaseClass1Rep[ZoneId, T](decodeObject[ZoneId, T](f.decode), f.encode)

  implicit def zoneOffsetRep[T](implicit f: Formatter[ZoneOffset, T]): CaseClass1Rep[ZoneOffset, T] =
    new CaseClass1Rep[ZoneOffset, T](decodeObject[ZoneOffset, T](f.decode), f.encode)

  implicit def zonedDateTimeRep[T](implicit f: Formatter[ZonedDateTime, T]): CaseClass1Rep[ZonedDateTime, T] =
    new CaseClass1Rep[ZonedDateTime, T](decodeObject[ZonedDateTime, T](f.decode), f.encode)

  private def decodeObject[Obj, Val](decode: Val => Either[DecodeError, Obj])(value: Val): Obj = {
    decode(value) match {
      case Left(DecodeError(msg, e)) => throw new IllegalArgumentException(msg, e)
      case Right(value)              => value
    }
  }
}

object TimeInstances {
  private[instances] val DayOfWeekFormat      = "ISO-8601 standard, from 1 (Monday) to 7 (Sunday)"
  private[instances] val DurationFormat       = "ISO-8601 standard format e.g. PT20.345S"
  private[instances] val InstantFormat        = "ISO-8601 standard format e.g. 2007-12-03T10:15:30.00Z"
  private[instances] val LocalDateFormat      = "ISO-8601 standard format e.g. 2007-12-03"
  private[instances] val LocalDateTimeFormat  = "ISO-8601 standard format e.g. 2007-12-03T10:15:30"
  private[instances] val LocalTimeFormat      = "ISO-8601 standard format e.g. 10:15:30"
  private[instances] val MonthDayFormat       = "ISO-8601 standard format e.g. --12-03"
  private[instances] val MonthFormat          = "ISO-8601 standard, from 1 (January) to 12 (December)"
  private[instances] val OffsetDateTimeFormat = "ISO-8601 standard format e.g. 2011-12-03T10:15:30+01:00"
  private[instances] val OffsetTimeFormat     = "ISO-8601 standard format e.g. 10:15:30+01:00"
  private[instances] val PeriodFormat         = "ISO-8601 standard format e.g. P2Y"
  private[instances] val YearFormat           = "ISO-8601 standard format e.g. 2007"
  private[instances] val YearMonthFormat      = "ISO-8601 standard format e.g. 2011-12"
  private[instances] val ZoneOffsetFormat     = "ISO-8601 standard format e.g. +01:00"
  private[instances] val ZonedDateTimeFormat  = "ISO-8601 standard format extended with zone e.g. 2011-12-03T10:15:30+01:00[Europe/Warsaw]"
  private[instances] val ZoneIdFormat         = "IANA standard format e.g. Europe/Warsaw"

  private[instances] def exceptionMessage[Obj, Val](clazz: Class[Obj], value: Val, format: String): String =
    s"${clazz.getName} cannot be parsed from $value – should be in format $format"

  case class DecodeError(msg: String, e: Throwable)

  trait Formatter[Obj, Val] {
    def encode(obj: Obj): Val
    def decode(value: Val): Either[DecodeError, Obj]
  }

  private def tryParse[Obj, Val](parse: Val => Obj, value: Val, clazz: Class[Obj], format: String) = {
    try {
      Right(parse(value))
    } catch {
      case e: DateTimeException => Left(DecodeError(exceptionMessage(clazz, value, format), e))
      case e: Throwable         => throw e
    }
  }

  trait DayOfWeekNumber {
    implicit val dayOfWeekFormatter: Formatter[DayOfWeek, Int] = new Formatter[DayOfWeek, Int] {
      override def encode(obj: DayOfWeek): Int = obj.getValue
      override def decode(value: Int): Either[DecodeError, DayOfWeek] =
        tryParse[DayOfWeek, Int](DayOfWeek.of, value, classOf[DayOfWeek], DayOfWeekFormat)
    }
  }

  trait InstantString {
    implicit val instantFormatter: Formatter[Instant, String] = new Formatter[Instant, String] {
      override def encode(obj: Instant): String = obj.toString
      override def decode(value: String): Either[DecodeError, Instant] =
        tryParse[Instant, String](Instant.parse, value, classOf[Instant], InstantFormat)
    }
  }

  trait DurationString {
    implicit val durationFormatter: Formatter[Duration, String] = new Formatter[Duration, String] {
      override def encode(obj: Duration): String = obj.toString
      override def decode(value: String): Either[DecodeError, Duration] =
        tryParse[Duration, String](Duration.parse, value, classOf[Duration], DurationFormat)
    }
  }

  trait LocalDateString {
    implicit val localDateFormatter: Formatter[LocalDate, String] = new Formatter[LocalDate, String] {
      override def encode(obj: LocalDate): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, LocalDate] =
        tryParse[LocalDate, String](LocalDate.parse(_, formatter), value, classOf[LocalDate], LocalDateFormat)
    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
  }

  trait LocalDateTimeString {
    implicit val localDateTimeFormatter: Formatter[LocalDateTime, String] = new Formatter[LocalDateTime, String] {
      override def encode(obj: LocalDateTime): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, LocalDateTime] =
        tryParse[LocalDateTime, String](LocalDateTime.parse(_, formatter), value, classOf[LocalDateTime], LocalDateTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  }

  trait LocalTimeString {
    implicit val localTimeFormatter: Formatter[LocalTime, String] = new Formatter[LocalTime, String] {
      override def encode(obj: LocalTime): String = obj.toString
      override def decode(value: String): Either[DecodeError, LocalTime] =
        tryParse[LocalTime, String](LocalTime.parse(_, formatter), value, classOf[LocalTime], LocalTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
  }

  trait MonthNumber {
    implicit val monthFormatter: Formatter[Month, Int] = new Formatter[Month, Int] {
      override def encode(obj: Month): Int = obj.getValue
      override def decode(value: Int): Either[DecodeError, Month] =
        tryParse[Month, Int](Month.of, value, classOf[Month], MonthFormat)
    }
  }

  trait MonthDayString {
    implicit val monthDayFormatter: Formatter[MonthDay, String] = new Formatter[MonthDay, String] {
      override def encode(obj: MonthDay): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, MonthDay] =
        tryParse[MonthDay, String](MonthDay.parse(_, formatter), value, classOf[MonthDay], MonthDayFormat)
    }
    private val formatter = DateTimeFormatter.ofPattern("--MM-dd")
  }

  trait OffsetDateTimeString {
    implicit val offsetDateTimeFormatter: Formatter[OffsetDateTime, String] = new Formatter[OffsetDateTime, String] {
      override def encode(obj: OffsetDateTime): String = obj.toString
      override def decode(value: String): Either[DecodeError, OffsetDateTime] =
        tryParse[OffsetDateTime, String](OffsetDateTime.parse(_, formatter), value, classOf[OffsetDateTime], OffsetDateTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  }

  trait OffsetTimeString {
    implicit val offsetTimeFormatter: Formatter[OffsetTime, String] = new Formatter[OffsetTime, String] {
      override def encode(obj: OffsetTime): String = obj.toString
      override def decode(value: String): Either[DecodeError, OffsetTime] =
        tryParse[OffsetTime, String](OffsetTime.parse(_, formatter), value, classOf[OffsetTime], OffsetTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_OFFSET_TIME
  }

  trait PeriodString {
    implicit val periodFormatter: Formatter[Period, String] = new Formatter[Period, String] {
      override def encode(obj: Period): String = obj.toString
      override def decode(value: String): Either[DecodeError, Period] =
        tryParse[Period, String](Period.parse, value, classOf[Period], PeriodFormat)
    }
  }

  trait YearString {
    implicit val yearFormatter: Formatter[Year, String] = new Formatter[Year, String] {
      override def encode(obj: Year): String = obj.toString
      override def decode(value: String): Either[DecodeError, Year] =
        tryParse[Year, String](Year.parse, value, classOf[Year], YearFormat)
    }
  }

  trait YearMonthString {
    implicit val yearMonthFormatter: Formatter[YearMonth, String] = new Formatter[YearMonth, String] {
      override def encode(obj: YearMonth): String = obj.toString
      override def decode(value: String): Either[DecodeError, YearMonth] =
        tryParse[YearMonth, String](YearMonth.parse, value, classOf[YearMonth], YearMonthFormat)
    }
    private val formatter = DateTimeFormatter.ofPattern("uuuu-MM")
  }

  trait ZoneIdString {
    implicit val zoneIdFormatter: Formatter[ZoneId, String] = new Formatter[ZoneId, String] {
      override def encode(obj: ZoneId): String = obj.toString
      override def decode(value: String): Either[DecodeError, ZoneId] =
        tryParse[ZoneId, String](ZoneId.of, value, classOf[ZoneId], ZoneIdFormat)
    }
  }

  trait ZoneOffsetString {
    implicit val zoneOffsetFormatter: Formatter[ZoneOffset, String] = new Formatter[ZoneOffset, String] {
      override def encode(obj: ZoneOffset): String = obj.toString
      override def decode(value: String): Either[DecodeError, ZoneOffset] =
        tryParse[ZoneOffset, String](ZoneOffset.of, value, classOf[ZoneOffset], ZoneOffsetFormat)
    }
  }

  trait ZonedDateTimeString {
    implicit val zonedDateTimeFormatter: Formatter[ZonedDateTime, String] = new Formatter[ZonedDateTime, String] {
      override def encode(obj: ZonedDateTime): String = obj.toString
      override def decode(value: String): Either[DecodeError, ZonedDateTime] =
        tryParse[ZonedDateTime, String](ZonedDateTime.parse(_, formatter), value, classOf[ZonedDateTime], ZonedDateTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
  }

  trait DurationMinutes extends TimeInstances {
    implicit val durationMinutesRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofMinutes, _.toMinutes)
  }

  trait DurationSeconds extends TimeInstances {
    implicit val durationMinutesRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofSeconds, _.getSeconds)
  }

  trait DurationMillis extends TimeInstances {
    implicit val durationMillisRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofMillis, _.toMillis)
  }

  trait DurationNanos extends TimeInstances {
    implicit val durationNanosRep: CaseClass1Rep[Duration, Long] =
      new CaseClass1Rep[Duration, Long](Duration.ofNanos, _.toNanos)
  }

  trait InstantEpochSecond extends TimeInstances {
    implicit val instantEpochSecondRep: CaseClass1Rep[Instant, Long] =
      new CaseClass1Rep[Instant, Long](Instant.ofEpochSecond, _.getEpochSecond)
  }

  trait InstantEpochMilli extends TimeInstances {
    implicit val instantEpochMilliRep: CaseClass1Rep[Instant, Long] =
      new CaseClass1Rep[Instant, Long](Instant.ofEpochMilli, _.toEpochMilli)
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
