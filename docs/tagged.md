---
sidebar_position: 4
title: Tagged Types
---

# kebs-tagged (Scala 2)

Compile-time type distinctions without runtime overhead.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-tagged" % kebsVersion
libraryDependencies += "pl.iterators" %% "kebs-tagged-meta" % kebsVersion // for @tagged code generation (Scala 2 only)
```

## Basic tagging

```scala
import pl.iterators.kebs.tagged._

trait UserIdTag
trait PurchaseIdTag

val userId: Int @@ UserIdTag = 10.taggedWith[UserIdTag]
val purchaseId: Int @@ PurchaseIdTag = 10.@@[PurchaseIdTag]

// Tag all elements in a collection
val userIds: List[Int @@ UserIdTag] = List(10, 15, 20).@@@[UserIdTag]
```

## Stacking tags

You can add additional tags to already-tagged values:

```scala
val both: Int @@ (UserIdTag with ActiveTag) = userId.andTaggedWith[ActiveTag]
// or: userId.+@[ActiveTag]

// Inside containers:
val bothList: List[Int @@ (UserIdTag with ActiveTag)] = userIds.andTaggedWithF[ActiveTag]
// or: userIds.+@@[ActiveTag]
```

## Mapping tagged values

Transform the underlying value while preserving the tag:

```scala
val doubled: Int @@ UserIdTag = userId.map(_ * 2)
```

## Code generation with @tagged

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

## Slick support

```scala
import pl.iterators.kebs.tagged.slick.KebsTaggedSlickSupport

object MyProfile extends ExPostgresDriver {
  override val api: API = new API {}
  trait API extends super.API with KebsTaggedImplicits  // inner trait of KebsTaggedSlickSupport
}
```

## Circe support

```scala
import pl.iterators.kebs.tagged.circe.KebsTaggedCirce
// or: import pl.iterators.kebs.tagged.circe._
```

Provides automatic `Codec` instances for common tagged base types: `String`, `UUID`, `BigDecimal`, `Int`, and `Json`.
