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
