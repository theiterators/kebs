---
sidebar_position: 6
title: Play JSON
---

# kebs-play-json

Flat `Format` instances for Play JSON value classes.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-play-json" % kebsVersion
```

## Usage

Play JSON's `Json.format[CC]` macro handles multi-field case classes well, but 1-element case classes still need manual flat formats. Kebs handles that automatically:

```scala
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass

object ThingProtocol extends KebsPlayJson with CaseClass1ToValueClass {
  // only multi-field classes need explicit formats
  implicit val locationJsonFormat = Json.format[Location]
  implicit val thingJsonFormat    = Json.format[Thing]
}
```

Value class formats (e.g. `ThingId`, `TagId`) are derived automatically — no `jsonFlatFormat` boilerplate needed.
