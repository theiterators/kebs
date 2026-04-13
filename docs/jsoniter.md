---
sidebar_position: 12
title: Jsoniter
---

# kebs-jsoniter

Automatic `JsonValueCodec` derivation for [jsoniter-scala](https://github.com/plokhotnyuk/jsoniter-scala).

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-jsoniter" % kebsVersion
```

## Basic usage

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniter

object ThingProtocol extends KebsJsoniter
```

Or use the package object directly:

```scala
import pl.iterators.kebs.jsoniter._
```

This derives `JsonValueCodec` for:
- 1-element case classes (flat format)
- Multi-field case classes
- Case classes with > 22 fields
- Types with an `InstanceConverter` (e.g. `UUID`, `java.time` types — see [instances](instances.md))

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniterSnakified

object ThingProtocol extends KebsJsoniterSnakified

// or via package object:
import pl.iterators.kebs.jsoniter.snakified._
```

Capitalized variant:

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniterCapitalized
// or: import pl.iterators.kebs.jsoniter.capitalized._
```

## Enum support

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterValueEnums}

object ThingProtocol extends KebsJsoniter with KebsJsoniterEnums with KebsJsoniterValueEnums
```

Or via the enums package object:

```scala
import pl.iterators.kebs.jsoniter.enums._            // default casing + value enums
import pl.iterators.kebs.jsoniter.enums.uppercase._   // UPPERCASE + value enums
import pl.iterators.kebs.jsoniter.enums.lowercase._   // lowercase + value enums
```

Enum codecs are case-insensitive on read by default and preserve the original name on write.

## Instance support (java.time, UUID, etc.)

Mix in `kebs-instances` traits to get automatic codecs for common types:

```scala
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.UtilInstances

object ThingProtocol extends KebsJsoniter with TimeInstances with UtilInstances
```
