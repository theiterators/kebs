---
sidebar_position: 14
title: ScalaCheck
---

# kebs-scalacheck

Automatic `Arbitrary` instance generation for ScalaCheck.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-scalacheck" % kebsVersion
```

## Usage

```scala
import pl.iterators.kebs.scalacheck._
```

Generates `Arbitrary` instances for:
- Value classes and single-field case classes
- Opaque types (Scala 3)
- Enums and value enums, from their values, when their support is in scope (`KebsEnum`, `KebsEnumeratum`, … — see [enums](enums.md))
- Common Java types: `Instant`, `ZonedDateTime`, `LocalDate`, `LocalDateTime`, `LocalTime`, `Duration`, `URI`, `URL`
- `String` (using `Gen.alphaNumStr`)

### `generate[T]()` utility

A convenience function to produce a single value without `.sample.get`:

```scala
import pl.iterators.kebs.scalacheck._

val user = generate[User]()
val seeded = generate[User](seed = Seed(42L))
```
