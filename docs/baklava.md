---
sidebar_position: 17
title: Baklava
---

# kebs-baklava

Integration with [Baklava](https://github.com/theiterators/baklava) for schema and parameter support.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-baklava" % kebsVersion
```

## Usage

```scala
import pl.iterators.kebs.baklava.params.KebsBaklavaParams
import pl.iterators.kebs.baklava.schema.KebsBaklavaSchema
import pl.iterators.kebs.baklava.params.enums.KebsBaklavaEnumsParams
import pl.iterators.kebs.baklava.schema.enums.KebsBaklavaEnumsSchema
```

Supports value classes, instance converter types, enums, and value enums. For value enum params/schema, use `KebsBaklavaValueEnumsParams` / `KebsBaklavaValueEnumsSchema`.

### Enum casing

```scala
import pl.iterators.kebs.baklava.params.enums.uppercase._   // UPPERCASE
import pl.iterators.kebs.baklava.params.enums.lowercase._   // lowercase
import pl.iterators.kebs.baklava.schema.enums.uppercase._
import pl.iterators.kebs.baklava.schema.enums.lowercase._
```

Types with an `InstanceConverter` also get format-aware schema derivation (e.g. `"date"`, `"date-time"`, `"uri"`, `"uuid"` JSON Schema formats).
