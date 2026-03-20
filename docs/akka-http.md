---
sidebar_position: 8
title: Akka HTTP / Pekko HTTP
---

# kebs-akka-http / kebs-pekko-http

Automatic unmarshallers for Akka HTTP and Pekko HTTP.

## Setup

```scala
// Akka HTTP (Scala 2 only)
libraryDependencies += "pl.iterators" %% "kebs-akka-http" % kebsVersion

// Pekko HTTP (Scala 2 & 3)
libraryDependencies += "pl.iterators" %% "kebs-pekko-http" % kebsVersion
```

## Usage

Provides automatic `FromStringUnmarshaller` for value classes, enums, and instance types in `parameters`, `path`, and `entity` directives:

```scala
import pl.iterators.kebs.pekkohttp.unmarshallers.KebsPekkoHttpUnmarshallers
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong

// For Scala 3 native enums, use instead:
// import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

case class Offset(value: Int) extends AnyVal
case class Limit(value: Int) extends AnyVal

val route = get {
  parameters("sortBy".as[Column], "order".as[SortOrder], "offset".as[Offset], "limit".as[Limit]) {
    (sortBy, order, offset, limit) => // ...
  }
}
```

The Akka HTTP variant uses the same pattern with `KebsAkkaHttpUnmarshallers`:

```scala
import pl.iterators.kebs.akkahttp.unmarshallers.KebsAkkaHttpUnmarshallers
```
