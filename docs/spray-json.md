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
- **Flat format** for 1-element case classes, with `CaseClass1ToValueClass` mixed in (`case class ThingId(uuid: UUID)` serializes as just the UUID, not `{"uuid": "..."}`)
- **Automatic format** for case classes (generic, recursive, of any size) and sealed hierarchies
- **Case object** format (serializes as `{}`)

What exactly is derived, the configuration and the annotations are the same in all JSON modules: see [JSON derivation](json.md).

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

## NullOptions support

Mix in spray-json's `NullOptions` to serialize `None` as `null` instead of omitting the field:

```scala
import spray.json.NullOptions

object ThingProtocol extends DefaultJsonProtocol with KebsSprayJson with NullOptions
```

## Enum support

```scala
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonEnums, KebsSprayJsonValueEnums}

object ThingProtocol extends DefaultJsonProtocol with KebsSprayJson
    with KebsEnumeratum with KebsValueEnumeratum with KebsSprayJsonEnums with KebsSprayJsonValueEnums
```

`KebsEnum` and `KebsValueEnum` take the place of `KebsEnumeratum` and `KebsValueEnumeratum` for Scala 3 enums and `scala.Enumeration`.

For uppercase/lowercase enum serialization:

```scala
import pl.iterators.kebs.sprayjson.enums.{KebsSprayJsonEnumsUppercase, KebsSprayJsonEnumsLowercase}

object ThingProtocol extends DefaultJsonProtocol
    with KebsSprayJson with KebsEnumeratum with KebsSprayJsonEnumsUppercase with KebsSprayJsonValueEnums
```

Other naming styles: an implicit [`EnumNaming`](enums.md#naming-styles).

Or via the enums package object, which needs the enum's support too:

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

For alternative encodings (e.g. `Instant` as epoch millis instead of ISO string), see the [instances documentation](instances.md).

## Explicit formats

Formats are implicit, but can also be made explicitly. `jsonFormatN` and `jsonFormatSealed` always derive, with the configuration of their protocol, also when the type has a format. For example, to serialize a 1-field case class as an object (not flat):

```scala
case class Chapter(name: String)
implicit val chapterRootFormat: RootJsonFormat[Chapter] = jsonFormatN[Chapter]
```

An explicit format takes the configuration of its protocol, but looks value classes and converters up in scope, so import the protocol that has them.

`jsonFormatRec` is a deprecated alias of `jsonFormatN`: recursive types need nothing extra.
