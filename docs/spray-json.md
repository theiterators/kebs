---
sidebar_position: 9
title: Spray JSON
---

# kebs-spray-json

Automatic `JsonFormat` derivation for spray-json.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-spray-json" % kebsVersion
```

## Basic usage

Mix in `KebsSprayJson` to get automatic format derivation:

```scala
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json.DefaultJsonProtocol

object ThingProtocol extends DefaultJsonProtocol with KebsSprayJson
```

Or use the package object directly (it extends `DefaultJsonProtocol` + `KebsSprayJson`):

```scala
import pl.iterators.kebs.sprayjson._
```

This gives you:
- **Flat format** for 1-element case classes (`case class ThingId(uuid: UUID)` serializes as just the UUID, not `{"uuid": "..."}`)
- **Automatic format** for multi-field case classes (no need to call `jsonFormatN`)
- **Case object** format (serializes as `{}`)
- **Parametrized (generic) case classes**
- **Support for case classes with > 22 fields**

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.sprayjson.KebsSprayJsonSnakified

object ThingProtocol extends DefaultJsonProtocol with KebsSprayJsonSnakified

// or via package object:
import pl.iterators.kebs.sprayjson.snakified._
```

Capitalized variant:

```scala
import pl.iterators.kebs.sprayjson.KebsSprayJsonCapitalized
// or: import pl.iterators.kebs.sprayjson.capitalized._
```

Snakified/capitalized names are computed at compile time.

## NullOptions support

Mix in spray-json's `NullOptions` to serialize `None` as `null` instead of omitting the field:

```scala
object ThingProtocol extends DefaultJsonProtocol with KebsSprayJson with NullOptions
```

## Enum support

```scala
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonEnums, KebsSprayJsonValueEnums}

object ThingProtocol extends DefaultJsonProtocol
    with KebsSprayJson with KebsSprayJsonEnums with KebsSprayJsonValueEnums
```

For uppercase/lowercase enum serialization:

```scala
import pl.iterators.kebs.sprayjson.enums.{KebsSprayJsonEnumsUppercase, KebsSprayJsonEnumsLowercase}

object ThingProtocol extends DefaultJsonProtocol
    with KebsSprayJson with KebsSprayJsonEnumsUppercase with KebsSprayJsonValueEnums
```

Or via the enums package object:

```scala
import pl.iterators.kebs.sprayjson.enums._            // default casing
import pl.iterators.kebs.sprayjson.enums.uppercase._   // UPPERCASE
import pl.iterators.kebs.sprayjson.enums.lowercase._   // lowercase
```

## Instance support (java.time, UUID, etc.)

Mix in `kebs-instances` traits to get automatic `JsonFormat` for common types:

```scala
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.UtilInstances
import pl.iterators.kebs.instances.net.URIString

object ThingProtocol extends DefaultJsonProtocol
    with KebsSprayJson with TimeInstances with UtilInstances with URIString
```

This covers `Instant`, `LocalDate`, `ZonedDateTime`, `Duration`, `UUID`, `Currency`, `Locale`, `URI`, and more.

For alternative encodings (e.g. `Instant` as epoch millis instead of ISO string), see the [instances documentation](other.md#kebs-instances).

## Recursive formats

```scala
case class Thing(thingId: String, parent: Option[Thing])
implicit val thingFormat: RootJsonFormat[Thing] = jsonFormatRec[Thing]
```

## Overriding flat format

If you want a 1-field case class serialized as an object (not flat), redefine the implicit:

```scala
case class Chapter(name: String)
implicit val chapterRootFormat: RootJsonFormat[Chapter] = jsonFormatN[Chapter]
```
