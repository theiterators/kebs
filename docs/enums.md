---
sidebar_position: 5
title: Enums
---

# kebs-enum / kebs-enumeratum

Enum support modules providing `EnumLike` / `ValueEnumLike` instances.

## Setup

```scala
// Scala 3 native enums + scala.Enumeration
libraryDependencies += "pl.iterators" %% "kebs-enum" % kebsVersion

// Enumeratum (Scala 2 & 3)
libraryDependencies += "pl.iterators" %% "kebs-enumeratum" % kebsVersion
```

## Usage

```scala
// Scala 3 native enums and scala.Enumeration (the latter on Scala 2 too);
// KebsValueEnum on Scala 2: sealed hierarchies of case objects with a value
import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

// Enumeratum
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
```

The entries of an Enumeratum enum are those of its `values`.

## Naming styles

Every module that writes enums by name (circe, spray-json, play-json, jsoniter, doobie, slick, pekko-http, http4s, http4s-stir, baklava, pureconfig) takes an implicit `EnumNaming`:

```scala
import pl.iterators.kebs.core.enums.EnumNaming

implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase // DarkBlue <-> "DARK_BLUE"
```

There are `default` (names as they are), `uppercase`, `lowercase`, `snakeCase`, `screamingSnakeCase` and `kebabCase`. For another style, give a `Naming`:

```scala
import pl.iterators.kebs.core.naming.Naming

implicit val enumNaming: EnumNaming = EnumNaming(Naming.custom(_.reverse), caseInsensitive = false)
```

Reading is case-insensitive unless `caseInsensitive = false`; an exact match always wins. Have one `EnumNaming` per scope, or shadow it under the same name.

The `uppercase` and `lowercase` presets of the modules keep their naming: they do not read an implicit `EnumNaming`.

## Value enums

For value enums, extend entries with `ValueEnumLikeEntry`:

```scala
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

// Scala 3
enum ColorRGB(val value: Int) extends ValueEnumLikeEntry[Int] {
  case Red extends ColorRGB(0xFF0000)
  case Green extends ColorRGB(0x00FF00)
  case Blue extends ColorRGB(0x0000FF)
}

// Enumeratum — supports Int, Long, Short, Byte, and String value types
import enumeratum.values._

sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
object LibraryItem extends IntEnum[LibraryItem] {
  case object Book extends LibraryItem(1)
  case object Movie extends LibraryItem(2)
  val values = findValues
}
```
