---
sidebar_position: 16
title: JSON Schema
---

# kebs-jsonschema (Scala 2 only)

JSON Schema generation via [scala-jsonschema](https://github.com/andyglow/scala-jsonschema).

**Deprecated in kebs 3.0**, to be removed in a later release: scala-jsonschema is no longer maintained (its last release was in June 2023).

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-jsonschema" % kebsVersion
```

## Usage

```scala
import pl.iterators.kebs.jsonschema.{KebsJsonSchema, JsonSchemaWrapper}

object Sample extends KebsJsonSchema {
  val schema = implicitly[JsonSchemaWrapper[MyType]].schema
}
```

Includes pre-built `Predef` instances for `ZonedDateTime`, `YearMonth`, `URL`, and `URI` (via `KebsJsonSchemaPredefs`, mixed in automatically).
