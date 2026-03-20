---
sidebar_position: 11
title: Circe
---

# kebs-circe

Automatic `Encoder` / `Decoder` derivation for Circe.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-circe" % kebsVersion
```

## Basic usage

```scala
import pl.iterators.kebs.circe.KebsCirce

object ThingProtocol extends KebsCirce
```

Or use the package object directly:

```scala
import pl.iterators.kebs.circe._
```

This derives encoders/decoders for:
- 1-element case classes (flat format)
- Multi-field case classes
- Case classes with > 22 fields
- Types with an `InstanceConverter` (e.g. `UUID`, `java.time` types — see [instances](instances.md))

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.circe.KebsCirceSnakified

object ThingProtocol extends KebsCirceSnakified

// or via package object:
import pl.iterators.kebs.circe.snakified._
```

Capitalized variant:

```scala
import pl.iterators.kebs.circe.KebsCirceCapitalized
// or: import pl.iterators.kebs.circe.capitalized._
```

In Scala 3, remember to import `given` instances:

```scala
object KebsProtocol extends KebsCirceSnakified
import KebsProtocol.{given, *}
```

In Scala 3, you can also override `configuration: Configuration` for custom field-name transformations.

## Enum support

```scala
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceValueEnums}

object ThingProtocol extends KebsCirce with KebsCirceEnums with KebsCirceValueEnums
```

Or via the enums package object:

```scala
import pl.iterators.kebs.circe.enums._            // default casing + value enums
import pl.iterators.kebs.circe.enums.uppercase._   // UPPERCASE + value enums
import pl.iterators.kebs.circe.enums.lowercase._   // lowercase + value enums
```

## Instance support (java.time, UUID, etc.)

Mix in `kebs-instances` traits to get automatic codecs for common types:

```scala
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.UtilInstances
import pl.iterators.kebs.instances.net.URIString

object ThingProtocol extends KebsCirce with TimeInstances with UtilInstances with URIString
```

Or use the aggregate:

```scala
import pl.iterators.kebs.instances.KebsInstances

object ThingProtocol extends KebsCirce with KebsInstances
```

For alternative encodings (e.g. `Instant` as epoch millis), see the [instances documentation](instances.md).

## Scala 3 note

Recursive types require explicit `derives` due to a [circe issue](https://github.com/circe/circe/issues/1980):

```scala
case class R(a: Int, rs: Seq[R]) derives Decoder, Encoder.AsObject
```
