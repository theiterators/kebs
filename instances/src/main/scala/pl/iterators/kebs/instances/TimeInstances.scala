package pl.iterators.kebs.instances

import pl.iterators.kebs.instances.time.{DurationString, PeriodString, _}

trait TimeInstances
    extends InstantString
    with DayOfWeekInt
    with DurationString
    with LocalDateString
    with LocalDateTimeString
    with LocalTimeString
    with MonthDayString
    with MonthInt
    with OffsetDateTimeString
    with OffsetTimeString
    with PeriodString
    with YearMonthString
    with YearInt
    with ZonedDateTimeString
    with ZoneIdString
    with ZoneOffsetString

object TimeInstances extends TimeInstances
