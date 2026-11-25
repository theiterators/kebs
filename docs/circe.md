---
sidebar_position: 11
title: Circe
---

# kebs-circe

Automatic `Encoder` / `Decoder` derivation for Circe, on JVM, Scala.js and Scala Native.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-circe" % kebsVersion
```

kebs-circe does not depend on circe-generic. Add it yourself if you use `semiauto` or `@JsonCodec`.

## Basic usage

```scala
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass

object ThingProtocol extends KebsCirce with CaseClass1ToValueClass
import ThingProtocol._

case class ThingId(value: String)
case class Thing(id: ThingId, tags: List[String], size: Option[Int])

Thing(ThingId("x"), List("a"), None).asJson // {"id":"x","tags":["a"],"size":null}
```

Or use the package object directly:

```scala
import pl.iterators.kebs.circe._
```

There are codecs of case classes, sealed hierarchies, value classes (with `CaseClass1ToValueClass`) and types with an `InstanceConverter`. What exactly is derived, the configuration and the annotations are the same in all JSON modules: see [JSON derivation](json.md).

Import one protocol per scope: on Scala 2, two of them cancel each other's implicits.

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

## Enum support

```scala
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceValueEnums}

object ThingProtocol extends KebsCirce with KebsCirceEnums with KebsCirceValueEnums
```

Or via the enums package object:

```scala
import pl.iterators.kebs.circe.enums._            // names as they are + value enums
import pl.iterators.kebs.circe.enums.uppercase._  // UPPERCASE + value enums
import pl.iterators.kebs.circe.enums.lowercase._  // lowercase + value enums
```

Other naming styles: an implicit [`EnumNaming`](enums.md#naming-styles).

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
