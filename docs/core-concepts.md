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
  def withNameUppercaseOnly(name: String): T
  def withNameLowercaseOnly(name: String): T
  def valueOf(name: String): T
  def fromOrdinal(ordinal: Int): T
  def indexOf(member: T): Int
  def names: immutable.Seq[String]
  def getName(e: T): String
  // ... and more
}
```

Instances are provided for Scala 3 `enum` types (via `kebs-enum`) and Enumeratum enums (via `kebs-enumeratum`).

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
  // ...
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

## How they fit together

Each integration module defines implicit derivation rules like:

```
ValueClassLike[VC, F1]  + Encoder[F1]         → Encoder[VC]
EnumLike[T]                                    → Encoder[T]  (via name)
ValueEnumLike[V, E]                            → Encoder[E]  (via value)
InstanceConverter[Obj, Val] + Encoder[Val]     → Encoder[Obj]
```

The same pattern applies to decoders, column types, unmarshallers, etc. You define your domain types, and Kebs provides the glue.
