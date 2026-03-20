---
sidebar_position: 10
title: Tagged & Opaque Types
---

# kebs-tagged / kebs-opaque

Support for tagged types (Scala 2) and opaque types (Scala 3).

## Setup

```scala
// Tagged types (Scala 2)
libraryDependencies += "pl.iterators" %% "kebs-tagged" % kebsVersion
libraryDependencies += "pl.iterators" %% "kebs-tagged-meta" % kebsVersion // for @tagged code generation (Scala 2 only)

// Opaque types (Scala 3)
libraryDependencies += "pl.iterators" %% "kebs-opaque" % kebsVersion
```

## Tagged types (Scala 2)

### Basic tagging

```scala
import pl.iterators.kebs.tagged._

trait UserIdTag
trait PurchaseIdTag

val userId: Int @@ UserIdTag = 10.taggedWith[UserIdTag]
val purchaseId: Int @@ PurchaseIdTag = 10.@@[PurchaseIdTag]

// Tag all elements in a collection
val userIds: List[Int @@ UserIdTag] = List(10, 15, 20).@@@[UserIdTag]
```

### Stacking tags

You can add additional tags to already-tagged values:

```scala
val both: Int @@ (UserIdTag with ActiveTag) = userId.andTaggedWith[ActiveTag]
// or: userId.+@[ActiveTag]

// Inside containers:
val bothList: List[Int @@ (UserIdTag with ActiveTag)] = userIds.andTaggedWithF[ActiveTag]
// or: userIds.+@@[ActiveTag]
```

### Mapping tagged values

Transform the underlying value while preserving the tag:

```scala
val doubled: Int @@ UserIdTag = userId.map(_ * 2)
```

### Code generation with @tagged

The `@tagged` annotation generates companion objects with constructors, validators, and `ValueClassLike` instances. It works on both objects and traits:

```scala
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta.tagged

@tagged object Tags {
  trait NameTag
  trait IdTag[+A]
  trait PositiveIntTag

  type Name = String @@ NameTag
  type Id[A] = Int @@ IdTag[A]

  type PositiveInt = Int @@ PositiveIntTag
  object PositiveInt {
    sealed trait Error
    case object Negative extends Error
    case object Zero extends Error
    def validate(i: Int) = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
  }
}

// Also works on traits (for sharing tag definitions as mixins):
@tagged trait SharedTags {
  trait EmailTag
  type Email = String @@ EmailTag
}
object MyDomain extends SharedTags
```

This generates `apply`, `from` methods, and `ValueClassLike` instances for each tagged type. Types with a `validate` method get validated constructors.

```scala
import Tags._

val name = Name("Someone")             // String @@ NameTag
val right = PositiveInt.from(10)       // Right(10)
val notRight = PositiveInt.from(-10)   // Left(Negative)
val value = PositiveInt(10)            // 10 (throws on invalid input)
```

### Conventions

- Tags must be empty traits (possibly generic)
- Tagged types must be aliases: `type X = SomeType @@ Tag`
- Validation methods must be named `validate`, take a single argument, and return `Either`

### Slick support for tagged types

```scala
import pl.iterators.kebs.tagged.slick.KebsTaggedSlickSupport

object MyProfile extends ExPostgresDriver {
  override val api: API = new API {}
  trait API extends super.API with KebsTaggedImplicits  // inner trait of KebsTaggedSlickSupport
}
```

### Circe support for tagged types

```scala
import pl.iterators.kebs.tagged.circe.KebsTaggedCirce
// or: import pl.iterators.kebs.tagged.circe._
```

Provides automatic `Codec` instances for common tagged base types: `String`, `UUID`, `BigDecimal`, `Int`, and `Json`.

## Opaque types (Scala 3)

### Basic usage

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

### With validation

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
