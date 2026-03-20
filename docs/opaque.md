---
sidebar_position: 3
title: Opaque Types
---

# kebs-opaque (Scala 3)

Zero-cost wrapper types with validation support.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-opaque" % kebsVersion
```

## Basic usage

```scala
import pl.iterators.kebs.opaque.Opaque

opaque type ISBN = String
object ISBN extends Opaque[ISBN, String]
```

This generates:
- `apply(unwrapped): OpaqueType` — constructor (throws on validation failure)
- `from(unwrapped): Either[String, OpaqueType]` — safe constructor
- `unsafe(unwrapped): OpaqueType` — bypasses validation entirely
- `.unwrap: Unwrapped` — extension method to extract the underlying value
- `given vcLike: ValueClassLike[OpaqueType, Unwrapped]` — for automatic typeclass derivation

## With validation

Override `validate` to add validation or normalization logic:

```scala
import pl.iterators.kebs.opaque.Opaque

opaque type ISBN = String
object ISBN extends Opaque[ISBN, String] {
  override protected def validate(unwrapped: String): Either[String, ISBN] = {
    val trimmed = unwrapped.trim
    if (trimmed.forall(_.isDigit) && trimmed.length == 10) Right(trimmed)
    else Left(s"Invalid ISBN: $trimmed")
  }
}

ISBN.from("1234567890")  // Right(ISBN("1234567890"))
ISBN.from("foo")          // Left("Invalid ISBN: foo")
ISBN("1234567890")        // ISBN("1234567890")
ISBN("foo")               // throws IllegalArgumentException

val isbn = ISBN("1234567890")
isbn.unwrap               // "1234567890"

ISBN.unsafe("anything")   // bypasses validate — use with care
```
