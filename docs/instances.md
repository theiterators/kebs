---
sidebar_position: 15
title: Instances
---

# kebs-instances

Pre-built `InstanceConverter` instances for common types. Used by other modules to support `java.time`, `java.util`, and `java.net` types.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-instances" % kebsVersion
```

## Import everything

```scala
import pl.iterators.kebs.instances.KebsInstances._
```

Or use the aggregate trait as a mixin:

```scala
import pl.iterators.kebs.instances.KebsInstances

object MyProtocol extends KebsCirce with KebsInstances
```

## Import by category

```scala
import pl.iterators.kebs.instances.TimeInstances   // all java.time types
import pl.iterators.kebs.instances.UtilInstances    // UUID, Currency, Locale
import pl.iterators.kebs.instances.NetInstances     // URI
```

## Import individual types

```scala
// java.time (default: String representation)
import pl.iterators.kebs.instances.time.InstantString
import pl.iterators.kebs.instances.time.LocalDateString
import pl.iterators.kebs.instances.time.LocalDateTimeString
import pl.iterators.kebs.instances.time.LocalTimeString
import pl.iterators.kebs.instances.time.ZonedDateTimeString
import pl.iterators.kebs.instances.time.OffsetDateTimeString
import pl.iterators.kebs.instances.time.OffsetTimeString
import pl.iterators.kebs.instances.time.DurationString
import pl.iterators.kebs.instances.time.PeriodString
import pl.iterators.kebs.instances.time.YearMonthString
import pl.iterators.kebs.instances.time.MonthDayString
import pl.iterators.kebs.instances.time.ZoneIdString
import pl.iterators.kebs.instances.time.ZoneOffsetString

// java.time (numeric representation)
import pl.iterators.kebs.instances.time.DayOfWeekInt   // 1-7 (ISO-8601)
import pl.iterators.kebs.instances.time.MonthInt        // 1-12
import pl.iterators.kebs.instances.time.YearInt

// java.util
import pl.iterators.kebs.instances.util.UUIDString
import pl.iterators.kebs.instances.util.CurrencyString
import pl.iterators.kebs.instances.util.LocaleString

// java.net
import pl.iterators.kebs.instances.net.URIString
```

## Alternative numeric encodings (mixins)

Override the default String representation with a numeric one by mixing in these traits:

**Duration:**
```scala
import pl.iterators.kebs.instances.time.mixins.DurationNanosLong    // Duration as nanoseconds
import pl.iterators.kebs.instances.time.mixins.DurationMillisLong   // Duration as milliseconds
import pl.iterators.kebs.instances.time.mixins.DurationSecondsLong  // Duration as seconds
import pl.iterators.kebs.instances.time.mixins.DurationMinutesLong  // Duration as minutes
```

**Instant:**
```scala
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong   // Instant as epoch millis
import pl.iterators.kebs.instances.time.mixins.InstantEpochSecondLong  // Instant as epoch seconds
```

**Period:**
```scala
import pl.iterators.kebs.instances.time.mixins.PeriodDays       // Period as days (Int)
import pl.iterators.kebs.instances.time.mixins.PeriodMonthsInt   // Period as months (Int)
import pl.iterators.kebs.instances.time.mixins.PeriodYearsInt    // Period as years (Int)
```

Example usage:

```scala
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.mixins.{InstantEpochMilliLong, DurationNanosLong}

// Instant as epoch millis, Duration as nanos, everything else as ISO String
object MyProtocol extends KebsCirce with TimeInstances with InstantEpochMilliLong with DurationNanosLong
```
