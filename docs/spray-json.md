---
sidebar_position: 5
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

This gives you:
- **Flat format** for 1-element case classes (`case class ThingId(uuid: UUID)` serializes as just the UUID, not `{"uuid": "..."}`)
- **Automatic format** for multi-field case classes (no need to call `jsonFormatN`)
- **Support for case classes with > 22 fields**

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.sprayjson.KebsSprayJsonSnakified

object ThingProtocol extends DefaultJsonProtocol with KebsSprayJsonSnakified
// or
import pl.iterators.kebs.sprayjson.KebsSprayJsonCapitalized

object ThingProtocol extends DefaultJsonProtocol with KebsSprayJsonCapitalized
```

Snakified names are computed at compile time.

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
