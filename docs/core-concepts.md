---
sidebar_position: 2
title: Core Concepts
---

# Core Concepts

Kebs is built on four typeclasses. Each integration module (circe, slick, akka-http, etc.) provides implicit derivation rules that delegate to these.

## ValueClassLike[VC, F1]

Represents an isomorphism between a wrapper type `VC` and its underlying type `F1`.

```scala
final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1)
```

Instances are derived automatically for:
- Value classes (`case class Foo(value: String) extends AnyVal`)
- Single-field case classes
- Tagged types (`String @@ MyTag`)
- Opaque types (Scala 3)

To enable derivation, mix in `CaseClass1ToValueClass` (or import from its companion object):

```scala
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass

// as a mixin
class MyService extends CaseClass1ToValueClass { ... }

// or as an import
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._
```

Integration modules use `ValueClassLike` to bridge wrapper types to their underlying representations — e.g. a `BaseColumnType[VC]` is derived from `BaseColumnType[F1]` + `ValueClassLike[VC, F1]`.

## EnumLike[T]

Unified interface for enumeration types.

```scala
trait EnumLike[T] {
  def values: immutable.Seq[T]
  def valuesToNamesMap: Map[T, String]
  def withName(name: String): T
  def withNameOption(name: String): Option[T]
  def withNameIgnoreCase(name: String): T
  def withNameIgnoreCaseOption(name: String): Option[T]
  def withNameUppercaseOnly(name: String): T
  def withNameLowercaseOnly(name: String): T
  def valueOf(name: String): T
  def valueOfIgnoreCase(name: String): T
  def fromOrdinal(ordinal: Int): T
  def indexOf(member: T): Int
  def names: immutable.Seq[String]
  def getName(e: T): String
  def getNamesToValuesMap: Map[String, T]
  // ... and Option-returning variants
}
```

Instances are provided for Scala 3 `enum` types (via `kebs-enum`), Scala 2 `scala.Enumeration` (via `kebs-enum`), and Enumeratum enums (via `kebs-enumeratum`).

## ValueEnumLikeEntry[V] / ValueEnumLike[V, E]

For enumerations where each entry maps to a specific value (e.g. an `Int` or `String`) rather than its name.

```scala
trait ValueEnumLikeEntry[ValueType] {
  def value: ValueType
}

trait ValueEnumLike[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]] {
  def values: immutable.Seq[EntryType]
  def withValue(value: ValueType): EntryType
  def withValueOption(value: ValueType): Option[EntryType]
  def valueOf(value: ValueType): EntryType
  def valueOfOption(value: ValueType): Option[EntryType]
  def fromOrdinal(ordinal: Int): EntryType
  def indexOf(member: EntryType): Int
  def getValuesToEntriesMap: Map[ValueType, EntryType]
}
```

Useful when you need to store/serialize enums as numeric codes or other non-string values.

## InstanceConverter[Obj, Val]

Bidirectional conversion between a complex type and a simpler representation, with error handling.

```scala
trait InstanceConverter[Obj, Val] {
  def encode(obj: Obj): Val
  def decode(value: Val): Obj
}
```

Factory method:

```scala
InstanceConverter[UUID, String](_.toString, UUID.fromString, Some("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"))
```

The optional `format` string is used in error messages when `decode` fails (throws `DecodeErrorException`).

Unlike `ValueClassLike`, `InstanceConverter` is for types that are not simple wrappers — they have their own structure and parsing can fail.

The `kebs-instances` module provides pre-built converters for `java.time` types, `UUID`, `URI`, `Currency`, `Locale`, etc.

## Stdlib typeclass derivation

The `kebs-core` `support` package automatically derives standard Scala typeclasses for wrapper types:

```scala
import pl.iterators.kebs.core.support._
```

This provides:
- `Numeric[A]` — when `A` wraps a numeric type (enables `.sum`, `.min`, `.max`, arithmetic)
- `Integral[A]` — integer division for `Int`/`Long`-backed wrappers
- `Fractional[A]` — real division for `BigDecimal`/`Double`-backed wrappers
- `Ordering[A]` — sorting and comparison (via `Numeric`)
- `PartialOrdering[A]` — partial order derivation
- `Equiv[A]` — equivalence derivation

```scala
case class Price(amount: BigDecimal) extends AnyVal
// with kebs support._ in scope:
List(Price(3), Price(1), Price(2)).sorted  // works
List(Price(10), Price(20)).sum             // works
```

## How they fit together

Each integration module defines implicit derivation rules like:

```
ValueClassLike[VC, F1]  + Encoder[F1]         → Encoder[VC]
EnumLike[T]                                    → Encoder[T]  (via name)
ValueEnumLike[V, E]                            → Encoder[E]  (via value)
InstanceConverter[Obj, Val] + Encoder[Val]     → Encoder[Obj]
```

The same pattern applies to decoders, column types, unmarshallers, etc. You define your domain types, and Kebs provides the glue.
