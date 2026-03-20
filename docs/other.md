---
sidebar_position: 11
title: Other Integrations
---

# Other Integrations

## kebs-scalacheck

Automatic `Arbitrary` instance generation for ScalaCheck.

```scala
libraryDependencies += "pl.iterators" %% "kebs-scalacheck" % kebsVersion
```

```scala
import pl.iterators.kebs.scalacheck._
```

Generates `Arbitrary` instances for value classes, case classes, enums, and common Java types (`Instant`, `LocalDate`, `Duration`, `URI`, `URL`, etc.).

## kebs-pureconfig

PureConfig `ConfigReader` / `ConfigWriter` instances.

```scala
libraryDependencies += "pl.iterators" %% "kebs-pureconfig" % kebsVersion
```

```scala
import pl.iterators.kebs.pureconfig._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
```

## kebs-jsonschema (Scala 2 only)

JSON Schema generation via [scala-jsonschema](https://github.com/andyglow/scala-jsonschema).

```scala
libraryDependencies += "pl.iterators" %% "kebs-jsonschema" % kebsVersion
```

```scala
import pl.iterators.kebs.jsonschema.{KebsJsonSchema, JsonSchemaWrapper}

object Sample extends KebsJsonSchema {
  val schema = implicitly[JsonSchemaWrapper[MyType]].schema
}
```

## kebs-baklava

Integration with [Baklava](https://github.com/theiterators/baklava) for schema and parameter support.

```scala
libraryDependencies += "pl.iterators" %% "kebs-baklava" % kebsVersion
```

```scala
import pl.iterators.kebs.baklava.params.KebsBaklavaParams
import pl.iterators.kebs.baklava.schema.KebsBaklavaSchema
import pl.iterators.kebs.baklava.params.enums.KebsBaklavaEnumsParams
import pl.iterators.kebs.baklava.schema.enums.KebsBaklavaEnumsSchema
```

## kebs-instances

Pre-built `InstanceConverter` instances for common types. Used by other modules.

```scala
libraryDependencies += "pl.iterators" %% "kebs-instances" % kebsVersion
```

Import everything:

```scala
import pl.iterators.kebs.instances.KebsInstances._
```

Or import selectively:

```scala
import pl.iterators.kebs.instances.TimeInstances   // all java.time types
import pl.iterators.kebs.instances.UtilInstances    // UUID, Currency, Locale
import pl.iterators.kebs.instances.net.URIString    // just URI <-> String
import pl.iterators.kebs.instances.time.YearMonthString
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.instances.time.mixins.DurationNanosLong
```

### Custom format mixins

Default instances use ISO string formats. Mixins let you override specific types:

```scala
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.mixins.{InstantEpochMilliLong, DurationNanosLong}

// Instant as epoch millis (Long), Duration as nanos (Long), everything else as String
object MyInstances extends TimeInstances with InstantEpochMilliLong with DurationNanosLong
```

## kebs-enum / kebs-enumeratum

Enum support modules providing `EnumLike` / `ValueEnumLike` instances.

```scala
// Scala 3 native enums
libraryDependencies += "pl.iterators" %% "kebs-enum" % kebsVersion

// Enumeratum (Scala 2 & 3)
libraryDependencies += "pl.iterators" %% "kebs-enumeratum" % kebsVersion
```

```scala
// Scala 3
import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

// Enumeratum
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
```

For value enums, extend entries with `ValueEnumLikeEntry`:

```scala
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry

// Scala 3
enum ColorRGB(val value: Int) extends ValueEnumLikeEntry[Int] {
  case Red extends ColorRGB(0xFF0000)
  case Green extends ColorRGB(0x00FF00)
  case Blue extends ColorRGB(0x0000FF)
}

// Enumeratum
sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
object LibraryItem extends IntEnum[LibraryItem] {
  case object Book extends LibraryItem(1)
  case object Movie extends LibraryItem(2)
  val values = findValues
}
```
