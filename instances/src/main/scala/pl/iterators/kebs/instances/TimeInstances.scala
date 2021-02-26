package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.time._
import java.time.format.DateTimeFormatter
import scala.util.control.NonFatal

trait TimeInstances extends Instances {

  implicit def dayOfWeekRep[T](implicit f: InstancesFormatter[DayOfWeek, T]): CaseClass1Rep[DayOfWeek, T] =
    new CaseClass1Rep[DayOfWeek, T](decodeObject[DayOfWeek, T](f.decode), f.encode)

  implicit def durationRep[T](implicit f: InstancesFormatter[Duration, T]): CaseClass1Rep[Duration, T] =
    new CaseClass1Rep[Duration, T](decodeObject[Duration, T](f.decode), f.encode)

  implicit def instantRep[T](implicit f: InstancesFormatter[Instant, T]): CaseClass1Rep[Instant, T] =
    new CaseClass1Rep[Instant, T](decodeObject[Instant, T](f.decode), f.encode)

  implicit def localDateRep[T](implicit f: InstancesFormatter[LocalDate, T]): CaseClass1Rep[LocalDate, T] =
    new CaseClass1Rep[LocalDate, T](decodeObject[LocalDate, T](f.decode), f.encode)

  implicit def localDateTimeRep[T](implicit f: InstancesFormatter[LocalDateTime, T]): CaseClass1Rep[LocalDateTime, T] =
    new CaseClass1Rep[LocalDateTime, T](decodeObject[LocalDateTime, T](f.decode), f.encode)

  implicit def localTimeRep[T](implicit f: InstancesFormatter[LocalTime, T]): CaseClass1Rep[LocalTime, T] =
    new CaseClass1Rep[LocalTime, T](decodeObject[LocalTime, T](f.decode), f.encode)

  implicit def monthRep[T](implicit f: InstancesFormatter[Month, T]): CaseClass1Rep[Month, T] =
    new CaseClass1Rep[Month, T](decodeObject[Month, T](f.decode), f.encode)

  implicit def monthDayRep[T](implicit f: InstancesFormatter[MonthDay, T]): CaseClass1Rep[MonthDay, T] =
    new CaseClass1Rep[MonthDay, T](decodeObject[MonthDay, T](f.decode), f.encode)

  implicit def offsetDateTimeRep[T](implicit f: InstancesFormatter[OffsetDateTime, T]): CaseClass1Rep[OffsetDateTime, T] =
    new CaseClass1Rep[OffsetDateTime, T](decodeObject[OffsetDateTime, T](f.decode), f.encode)

  implicit def offsetTimeRep[T](implicit f: InstancesFormatter[OffsetTime, T]): CaseClass1Rep[OffsetTime, T] =
    new CaseClass1Rep[OffsetTime, T](decodeObject[OffsetTime, T](f.decode), f.encode)

  implicit def periodRep[T](implicit f: InstancesFormatter[Period, T]): CaseClass1Rep[Period, T] =
    new CaseClass1Rep[Period, T](decodeObject[Period, T](f.decode), f.encode)

  implicit def yearRep[T](implicit f: InstancesFormatter[Year, T]): CaseClass1Rep[Year, T] =
    new CaseClass1Rep[Year, T](decodeObject[Year, T](f.decode), f.encode)

  implicit def yearMonthRep[T](implicit f: InstancesFormatter[YearMonth, T]): CaseClass1Rep[YearMonth, T] =
    new CaseClass1Rep[YearMonth, T](decodeObject[YearMonth, T](f.decode), f.encode)

  implicit def zoneIdRep[T](implicit f: InstancesFormatter[ZoneId, T]): CaseClass1Rep[ZoneId, T] =
    new CaseClass1Rep[ZoneId, T](decodeObject[ZoneId, T](f.decode), f.encode)

  implicit def zoneOffsetRep[T](implicit f: InstancesFormatter[ZoneOffset, T]): CaseClass1Rep[ZoneOffset, T] =
    new CaseClass1Rep[ZoneOffset, T](decodeObject[ZoneOffset, T](f.decode), f.encode)

  implicit def zonedDateTimeRep[T](implicit f: InstancesFormatter[ZonedDateTime, T]): CaseClass1Rep[ZonedDateTime, T] =
    new CaseClass1Rep[ZonedDateTime, T](decodeObject[ZonedDateTime, T](f.decode), f.encode)

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

  /** Standard formats */
  trait DayOfWeekNumber extends TimeInstances {
    implicit val dayOfWeekFormatter: InstancesFormatter[DayOfWeek, Int] = new InstancesFormatter[DayOfWeek, Int] {
      override def encode(obj: DayOfWeek): Int = obj.getValue
      override def decode(value: Int): Either[DecodeError, DayOfWeek] =
        tryDecode[DayOfWeek, Int](DayOfWeek.of, value, classOf[DayOfWeek], DayOfWeekFormat)
    }
  }

  trait InstantString extends TimeInstances {
    implicit val instantFormatter: InstancesFormatter[Instant, String] = new InstancesFormatter[Instant, String] {
      override def encode(obj: Instant): String = obj.toString
      override def decode(value: String): Either[DecodeError, Instant] =
        tryDecode[Instant, String](Instant.parse, value, classOf[Instant], InstantFormat)
    }
  }

  trait DurationString extends TimeInstances {
    implicit val durationFormatter: InstancesFormatter[Duration, String] = new InstancesFormatter[Duration, String] {
      override def encode(obj: Duration): String = obj.toString
      override def decode(value: String): Either[DecodeError, Duration] =
        tryDecode[Duration, String](Duration.parse, value, classOf[Duration], DurationFormat)
    }
  }

  trait LocalDateString extends TimeInstances {
    implicit val localDateFormatter: InstancesFormatter[LocalDate, String] = new InstancesFormatter[LocalDate, String] {
      override def encode(obj: LocalDate): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, LocalDate] =
        tryDecode[LocalDate, String](LocalDate.parse(_, formatter), value, classOf[LocalDate], LocalDateFormat)
    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
  }

  trait LocalDateTimeString extends TimeInstances {
    implicit val localDateTimeFormatter: InstancesFormatter[LocalDateTime, String] = new InstancesFormatter[LocalDateTime, String] {
      override def encode(obj: LocalDateTime): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, LocalDateTime] =
        tryDecode[LocalDateTime, String](LocalDateTime.parse(_, formatter), value, classOf[LocalDateTime], LocalDateTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  }

  trait LocalTimeString extends TimeInstances {
    implicit val localTimeFormatter: InstancesFormatter[LocalTime, String] = new InstancesFormatter[LocalTime, String] {
      override def encode(obj: LocalTime): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, LocalTime] =
        tryDecode[LocalTime, String](LocalTime.parse(_, formatter), value, classOf[LocalTime], LocalTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
  }

  trait MonthNumber extends TimeInstances {
    implicit val monthFormatter: InstancesFormatter[Month, Int] = new InstancesFormatter[Month, Int] {
      override def encode(obj: Month): Int = obj.getValue
      override def decode(value: Int): Either[DecodeError, Month] =
        tryDecode[Month, Int](Month.of, value, classOf[Month], MonthFormat)
    }
  }

  trait MonthDayString extends TimeInstances {
    implicit val monthDayFormatter: InstancesFormatter[MonthDay, String] = new InstancesFormatter[MonthDay, String] {
      override def encode(obj: MonthDay): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, MonthDay] =
        tryDecode[MonthDay, String](MonthDay.parse(_, formatter), value, classOf[MonthDay], MonthDayFormat)
    }
    private val formatter = DateTimeFormatter.ofPattern("--MM-dd")
  }

  trait OffsetDateTimeString extends TimeInstances {
    implicit val offsetDateTimeFormatter: InstancesFormatter[OffsetDateTime, String] = new InstancesFormatter[OffsetDateTime, String] {
      override def encode(obj: OffsetDateTime): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, OffsetDateTime] =
        tryDecode[OffsetDateTime, String](OffsetDateTime.parse(_, formatter), value, classOf[OffsetDateTime], OffsetDateTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  }

  trait OffsetTimeString extends TimeInstances {
    implicit val offsetTimeFormatter: InstancesFormatter[OffsetTime, String] = new InstancesFormatter[OffsetTime, String] {
      override def encode(obj: OffsetTime): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, OffsetTime] =
        tryDecode[OffsetTime, String](OffsetTime.parse(_, formatter), value, classOf[OffsetTime], OffsetTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_OFFSET_TIME
  }

  trait PeriodString extends TimeInstances {
    implicit val periodFormatter: InstancesFormatter[Period, String] = new InstancesFormatter[Period, String] {
      override def encode(obj: Period): String = obj.toString
      override def decode(value: String): Either[DecodeError, Period] =
        tryDecode[Period, String](Period.parse, value, classOf[Period], PeriodFormat)
    }
  }

  trait YearString extends TimeInstances {
    implicit val yearFormatter: InstancesFormatter[Year, String] = new InstancesFormatter[Year, String] {
      override def encode(obj: Year): String = obj.toString
      override def decode(value: String): Either[DecodeError, Year] =
        tryDecode[Year, String](Year.parse, value, classOf[Year], YearFormat)
    }
  }

  trait YearMonthString extends TimeInstances {
    implicit val yearMonthFormatter: InstancesFormatter[YearMonth, String] = new InstancesFormatter[YearMonth, String] {
      override def encode(obj: YearMonth): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, YearMonth] =
        tryDecode[YearMonth, String](YearMonth.parse, value, classOf[YearMonth], YearMonthFormat)
    }
    private val formatter = DateTimeFormatter.ofPattern("uuuu-MM")
  }

  trait ZoneIdString extends TimeInstances {
    implicit val zoneIdFormatter: InstancesFormatter[ZoneId, String] = new InstancesFormatter[ZoneId, String] {
      override def encode(obj: ZoneId): String = obj.toString
      override def decode(value: String): Either[DecodeError, ZoneId] =
        tryDecode[ZoneId, String](ZoneId.of, value, classOf[ZoneId], ZoneIdFormat)
    }
  }

  trait ZoneOffsetString extends TimeInstances {
    implicit val zoneOffsetFormatter: InstancesFormatter[ZoneOffset, String] = new InstancesFormatter[ZoneOffset, String] {
      override def encode(obj: ZoneOffset): String = obj.toString
      override def decode(value: String): Either[DecodeError, ZoneOffset] =
        tryDecode[ZoneOffset, String](ZoneOffset.of, value, classOf[ZoneOffset], ZoneOffsetFormat)
    }
  }

  trait ZonedDateTimeString extends TimeInstances {
    implicit val zonedDateTimeFormatter: InstancesFormatter[ZonedDateTime, String] = new InstancesFormatter[ZonedDateTime, String] {
      override def encode(obj: ZonedDateTime): String = obj.format(formatter)
      override def decode(value: String): Either[DecodeError, ZonedDateTime] =
        tryDecode[ZonedDateTime, String](ZonedDateTime.parse(_, formatter), value, classOf[ZonedDateTime], ZonedDateTimeFormat)
    }
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
  }

  /** [[java.time.Duration]] mixins */
  trait DurationMinutesLong extends TimeInstances {
    implicit val durationMinutesFormatter: InstancesFormatter[Duration, Long] =
      InstancesFormatter.apply[Duration, Long](_.toMinutes, (value: Long) => Right(Duration.ofMinutes(value)))
  }

  trait DurationSecondsLong extends TimeInstances {
    implicit val durationMinutesFormatter: InstancesFormatter[Duration, Long] =
      InstancesFormatter.apply[Duration, Long](_.getSeconds, (value: Long) => Right(Duration.ofSeconds(value)))
  }

  trait DurationMillisLong extends TimeInstances {
    implicit val durationMillisFormatter: InstancesFormatter[Duration, Long] =
      InstancesFormatter.apply[Duration, Long](_.toMillis, (value: Long) => Right(Duration.ofMillis(value)))
  }

  trait DurationNanosLong extends TimeInstances {
    implicit val durationNanosFormatter: InstancesFormatter[Duration, Long] =
      InstancesFormatter.apply[Duration, Long](_.toNanos, (value: Long) => Right(Duration.ofNanos(value)))
  }

  /** [[java.time.Instant]] mixins */
  trait InstantEpochSecondLong extends TimeInstances {
    implicit val instantEpochSecondFormatter: InstancesFormatter[Instant, Long] =
      InstancesFormatter.apply[Instant, Long](_.getEpochSecond, (value: Long) => Right(Instant.ofEpochSecond(value)))
  }

  trait InstantEpochMilliLong extends TimeInstances {
    implicit val instantEpochMilliFormatter: InstancesFormatter[Instant, Long] =
      InstancesFormatter.apply[Instant, Long](_.toEpochMilli, (value: Long) => Right(Instant.ofEpochMilli(value)))
  }

  /** [[java.time.Period]] mixins */
  trait PeriodYearsInt extends TimeInstances {
    implicit val periodYearsFormatter: InstancesFormatter[Period, Int] =
      InstancesFormatter.apply[Period, Int](_.getYears, (value: Int) => Right(Period.ofYears(value)))
  }

  trait PeriodMonthsInt extends TimeInstances {
    implicit val periodMonthsFormatter: InstancesFormatter[Period, Int] =
      InstancesFormatter.apply[Period, Int](_.getMonths, (value: Int) => Right(Period.ofMonths(value)))
  }

  trait PeriodDays extends TimeInstances {
    implicit val periodDaysFormatter: InstancesFormatter[Period, Int] =
      InstancesFormatter.apply[Period, Int](_.getDays, (value: Int) => Right(Period.ofDays(value)))
  }
}
