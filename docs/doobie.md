---
sidebar_position: 4
title: Doobie
---

# kebs-doobie

Automatic `Meta` / `Get` / `Put` instances for Doobie.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-doobie" % kebsVersion
```

## Usage

```scala
import pl.iterators.kebs.doobie._
import pl.iterators.kebs.doobie.enums._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.instances.KebsInstances._
```

This provides Doobie `Meta` instances for:
- `ValueClassLike` instances (value classes, tagged types, opaque types)
- `InstanceConverter` instances (e.g. `UUID`, `java.time` types via `kebs-instances`)
- Enumeratum enums (Scala 2 & 3)
- Native enums (Scala 3)

### Array and collection support

In addition to scalar `Meta[A]`, kebs-doobie also derives:
- `Meta[Array[A]]` — for Postgres array columns
- `Meta[Array[Option[A]]]` — for arrays of nullable elements

Via Doobie's own machinery, this also enables `List[A]`, `Vector[A]`, etc.

### Enum casing strategies

The default `import pl.iterators.kebs.doobie.enums._` stores enums by their entry name. For case-normalized storage:

```scala
import pl.iterators.kebs.doobie.enums.uppercase._  // stores as UPPERCASE
import pl.iterators.kebs.doobie.enums.lowercase._  // stores as lowercase
```

### Value enums

Value enums (entries with a typed `.value`) are supported via `KebsDoobieValueEnums`, which is included in the `enums` package object. No extra import needed beyond `import pl.iterators.kebs.doobie.enums._`.

### Mixin style

Instead of wildcard imports, you can mix traits directly:

```scala
import pl.iterators.kebs.doobie.KebsDoobie
import pl.iterators.kebs.doobie.enums.{KebsDoobieEnums, KebsDoobieValueEnums}

class MyRepo extends KebsDoobie with KebsDoobieEnums with KebsDoobieValueEnums { ... }
```
