---
sidebar_position: 12
title: Akka HTTP / Pekko HTTP
---

# kebs-akka-http / kebs-pekko-http

Automatic unmarshallers and path matchers for Akka HTTP and Pekko HTTP.

## Setup

```scala
// Akka HTTP (Scala 2 only)
libraryDependencies += "pl.iterators" %% "kebs-akka-http" % kebsVersion

// Pekko HTTP (Scala 2 & 3)
libraryDependencies += "pl.iterators" %% "kebs-pekko-http" % kebsVersion
```

## Unmarshallers

Provides automatic `FromStringUnmarshaller` for value classes, enums, and instance types in `parameters`, `formFields`, and `entity` directives:

```scala
import pl.iterators.kebs.pekkohttp.unmarshallers.KebsPekkoHttpUnmarshallers
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong

// For Scala 3 native enums, use instead:
// import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

case class Offset(value: Int) extends AnyVal
case class Limit(value: Int) extends AnyVal

val route = get {
  parameters("sortBy".as[Column], "order".as[SortOrder], "offset".as[Offset], "limit".as[Limit]) {
    (sortBy, order, offset, limit) => // ...
  }
}

// Also works with formFields:
val formRoute = post {
  formFields("yearMonth".as[YearMonth]) { yearMonth => // ... }
}
```

For Akka HTTP, use `KebsAkkaHttpUnmarshallers` instead:

```scala
import pl.iterators.kebs.akkahttp.unmarshallers.KebsAkkaHttpUnmarshallers
```

## Path matchers

A separate `matchers` package provides typed path segment extraction via extension methods on `PathMatcher1`:

```scala
import pl.iterators.kebs.pekkohttp.matchers.KebsPekkoHttpMatchers
// or: import pl.iterators.kebs.pekkohttp.matchers._

// For Akka HTTP:
// import pl.iterators.kebs.akkahttp.matchers.KebsAkkaHttpMatchers
```

### `.as[T]` — value class wrapping

Converts a path segment via `ValueClassLike`:

```scala
path("user" / LongNumber.as[UserId])          // Long → UserId
path("item" / JavaUUID.as[ItemId])             // UUID → ItemId
path("name" / Segment.as[Name])               // String → Name
```

### `.to[T]` — instance conversion

Converts a path segment via `InstanceConverter`:

```scala
path("date" / Segment.to[ZonedDateTime])       // String → ZonedDateTime
path("day" / IntNumber.to[DayOfWeek])          // Int → DayOfWeek
path("ts" / LongNumber.to[Instant])            // Long → Instant (with InstantEpochMilliLong)
```

### `.asEnum[T]` — enum by name

Converts a string segment via `EnumLike` (case-insensitive):

```scala
path("greeting" / Segment.asEnum[Greeting])    // "hello" → Greeting.Hello
```

### `.asValueEnum[T]` — value enum by value

Converts a segment via `ValueEnumLike`:

```scala
path("item" / IntNumber.asValueEnum[LibraryItem])  // 1 → LibraryItem.Book
```

### Chaining

Matchers can be chained — first convert, then wrap:

```scala
path("uri" / Segment.to[URI].as[TaggedUri])    // String → URI → TaggedUri
```
