---
sidebar_position: 10
title: Play JSON
---

# kebs-play-json

Automatic `Reads` / `OWrites` / `Format` derivation for Play JSON.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-play-json" % kebsVersion
```

## Usage

```scala
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import play.api.libs.json._

object ThingProtocol extends KebsPlayJson with CaseClass1ToValueClass
import ThingProtocol._

case class ThingId(value: String)
case class Thing(id: ThingId, tags: List[String], size: Option[Int])

Json.toJson(Thing(ThingId("x"), List("a"), None)) // {"id":"x","tags":["a"]}
Json.parse(json).as[Thing]
```

Or use the package object directly:

```scala
import pl.iterators.kebs.playjson._
```

Case classes are written and read as by `Json.format` (`None` left out, the same paths and errors), but need no `Json.format` of their own. Formats you have, `Json.format` included, win. What exactly is derived, the configuration and the annotations are the same in all JSON modules: see [JSON derivation](json.md).

Sealed hierarchies are written as `{"Circle":{...}}`, as in the other modules, not with play's `_type`. `withDiscriminator("_type")` in the configuration adds a discriminator.

Import one protocol per scope: on Scala 2, two of them cancel each other's implicits.

## Explicit derivation

`deriveReads`, `deriveWrites` and `deriveFormat` always derive, with the configuration of their protocol:

```scala
object Thing {
  implicit val format: OFormat[Thing] = ThingProtocol.deriveFormat[Thing]
}
```

They also tell why a type cannot be derived. Through an implicit search, play's own "No Json serializer found" hides the reason.

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.playjson.snakified._
import pl.iterators.kebs.playjson.capitalized._
```

## Enum support

```scala
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.playjson.enums.{KebsPlayJsonEnums, KebsPlayJsonValueEnums}

object ThingProtocol extends KebsPlayJson with KebsPlayJsonEnums with KebsPlayJsonValueEnums
```

Naming styles: an implicit [`EnumNaming`](enums.md#naming-styles), or the presets in `enums.uppercase` and `enums.lowercase`.
