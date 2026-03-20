---
sidebar_position: 7
title: Slick
---

# kebs-slick

Automatic column type mappings for Slick.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-slick" % kebsVersion
```

## Value class mappings

Instead of writing manual `MappedColumnType.base` for every wrapper type, mix in `KebsSlickSupport`:

```scala
import pl.iterators.kebs.slick.KebsSlickSupport
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass

// With a custom driver (e.g. slick-pg):
object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with KebsSlickSupport with CaseClass1ToValueClass
}
```

This derives `BaseColumnType[CC]` for any single-field case class, tagged type, or opaque type. It also derives `List[CC]` column types for Postgres arrays.

### Column extension methods

Wrapped columns automatically get the extension methods of their underlying type:

- `Rep[CC]` where `CC` wraps `String` gets `StringColumnExtensionMethods` (`.toLowerCase`, `.like`, etc.)
- `Rep[CC]` where `CC` wraps a numeric type gets arithmetic and comparison methods
- `Rep[CC]` where `CC` wraps `Boolean` gets `&&`, `||`, etc.

You can also compare a `Rep[CC]` directly against its unwrapped base type without explicit conversion.

## Instance support (java.time, UUID, etc.)

When `kebs-instances` traits are mixed into the API, types with an `InstanceConverter` (e.g. `java.time.YearMonth`, `URI`, `UUID`) also get automatic column types and array support:

```scala
import pl.iterators.kebs.instances.time.YearMonthString

trait API extends super.API with KebsSlickSupport with CaseClass1ToValueClass with YearMonthString
```

## Postgres array support

Works automatically for value classes, enums, and instance types:

```scala
class ArrayTestTable(tag: Tag)
    extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
  def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def institutions         = column[List[Institution]]("institutions")
  def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")
  def *                    = (id, institutions, mktFinancialProducts)
}
```

## Postgres hstore support

Mix in `HStoreImplicits` alongside `KebsSlickSupport` and the appropriate instance traits:

```scala
import pl.iterators.kebs.instances.time.YearMonthString

object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
  override val api: APIWithHstore = new APIWithHstore {}
  trait APIWithHstore extends super.API with HStoreImplicits
      with KebsSlickSupport with CaseClass1ToValueClass with YearMonthString
}
```

Hstore columns gain rich query operators: `+>` (get value), `??` (key exists), `?&` (all keys exist), `@>` (contains), `@+` (concatenate), `--` (delete keys), `slice`, and more.

Value classes, instance types, and enums can all be used as hstore map keys and values. Custom types can participate by providing a `ToFromStringForHstore[T]` implicit.

## Enum support

For Enumeratum (Scala 2 & 3):

```scala
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}

trait API extends super.API with KebsSlickSupport with KebsEnumeratum with KebsValueEnumeratum
```

For Scala 3 native enums:

```scala
import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

trait API extends super.API with KebsSlickSupport with KebsEnum with KebsValueEnum
```

### Enum casing strategies

By default, enums are stored using their entry name. For lowercase or uppercase storage, use the casing variant inner traits instead of the default enum implicits. These are available as inner traits within `KebsSlickSupport` (e.g. `KebsLowercaseEnumImplicits`, `KebsUppercaseEnumImplicits`).

Enum types also work as Postgres array column types (`List[MyEnum]`) and as hstore map keys/values.
