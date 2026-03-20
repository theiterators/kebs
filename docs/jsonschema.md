---
sidebar_position: 13
title: JSON Schema
---

# kebs-jsonschema (Scala 2 only)

JSON Schema generation via [scala-jsonschema](https://github.com/andyglow/scala-jsonschema).

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
