---
sidebar_position: 7
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

This derives encoders/decoders for:
- 1-element case classes (flat format)
- Multi-field case classes
- Case classes with > 22 fields

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.circe.KebsCirceSnakified

object ThingProtocol extends KebsCirceSnakified

// or
import pl.iterators.kebs.circe.KebsCirceCapitalized

object ThingProtocol extends KebsCirceCapitalized
```

In Scala 3, remember to import `given` instances:

```scala
object KebsProtocol extends KebsCirceSnakified
import KebsProtocol.{given, *}
```

## Enum support

```scala
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.circe.enums.{KebsCirceEnums, KebsCirceValueEnums}

object ThingProtocol extends KebsCirce with KebsCirceEnums with KebsCirceValueEnums
```

For uppercase/lowercase:

```scala
import pl.iterators.kebs.circe.enums.{KebsCirceEnumsUppercase, KebsCirceEnumsLowercase}
```

## Scala 3 note

Recursive types require explicit `derives` due to a [circe issue](https://github.com/circe/circe/issues/1980):

```scala
case class R(a: Int, rs: Seq[R]) derives Decoder, Encoder.AsObject
```
