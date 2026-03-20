---
sidebar_position: 5
title: Enums
---

# kebs-enum / kebs-enumeratum

Enum support modules providing `EnumLike` / `ValueEnumLike` instances.

## Setup

```scala
// Scala 3 native enums + Scala 2 scala.Enumeration
libraryDependencies += "pl.iterators" %% "kebs-enum" % kebsVersion

// Enumeratum (Scala 2 & 3)
libraryDependencies += "pl.iterators" %% "kebs-enumeratum" % kebsVersion
```

## Usage

```scala
// Scala 3 native enums
import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

// Scala 2 scala.Enumeration
import pl.iterators.kebs.enums.KebsEnum

// Enumeratum
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
```

**Note:** `KebsValueEnum` is Scala 3 only. On Scala 2 with Enumeratum, use `KebsValueEnumeratum`.

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
sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
object LibraryItem extends IntEnum[LibraryItem] {
  case object Book extends LibraryItem(1)
  case object Movie extends LibraryItem(2)
  val values = findValues
}
```
