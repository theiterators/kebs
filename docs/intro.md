---
sidebar_position: 1
title: Introduction
---

# Kebs

##### Scala library to eliminate boilerplate

[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)](https://central.sonatype.com/artifact/pl.iterators/kebs-core_2.13)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://github.com/theiterators/kebs/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/theiterators/kebs/actions/workflows/ci.yml?query=branch%3Amaster)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com).

## What is Kebs?

Kebs automatically derives typeclass instances (JSON codecs, DB column mappings, HTTP unmarshallers, etc.) for your domain types so you don't have to write them by hand. It supports Scala 2.13 and Scala 3.

Without Kebs, every wrapper type needs manual integration for every library:

```scala
case class UserId(value: String) extends AnyVal

// Slick
implicit val userIdColumnType: BaseColumnType[UserId] =
  MappedColumnType.base(_.value, UserId.apply)

// Circe
implicit val userIdEncoder: Encoder[UserId] = Encoder[String].contramap(_.value)
implicit val userIdDecoder: Decoder[UserId] = Decoder[String].map(UserId.apply)

// ... repeat for every type × every library
```

With Kebs, you mix in a trait and all of that is derived at compile time.

## Supported type patterns

### Value classes (Scala 2 & 3)

```scala
case class UserId(value: String) extends AnyVal
case class Price(amount: BigDecimal) extends AnyVal
```

### Tagged types (Scala 2, via kebs-tagged / kebs-tagged-meta)

```scala
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta.tagged

@tagged object Tags {
  trait UserIdTag
  type UserId = String @@ UserIdTag
}
```

### Opaque types (Scala 3, via kebs-opaque)

```scala
import pl.iterators.kebs.opaque._

opaque type UserId = String
object UserId extends Opaque[UserId, String]
```

### Enumerations

Scala 3 native enums (via `kebs-enum`) and Enumeratum (via `kebs-enumeratum`):

```scala
// Scala 3
enum Status { case Active, Inactive, Pending }

// Enumeratum (Scala 2 & 3)
sealed trait Priority extends EnumEntry
object Priority extends Enum[Priority] {
  case object High extends Priority
  case object Medium extends Priority
  case object Low extends Priority
  val values = findValues
}
```

## Library integrations

### Database

| Module | What you get |
|---|---|
| `kebs-slick` | Automatic `BaseColumnType` mappings, Postgres array & hstore support |
| `kebs-doobie` | `Meta` / `Get` / `Put` instances |

### JSON

| Module | What you get |
|---|---|
| `kebs-spray-json` | `JsonFormat` derivation (flat format for 1-field case classes, `>22` field support, snakified/capitalized) |
| `kebs-play-json` | `Format` instances for value classes |
| `kebs-circe` | `Encoder` / `Decoder` derivation (snakified/capitalized) |

### HTTP

| Module | What you get |
|---|---|
| `kebs-akka-http` | `FromStringUnmarshaller` for value classes, enums, instances |
| `kebs-pekko-http` | Same as akka-http, for Pekko |
| `kebs-http4s` | Path/query parameter codecs |
| `kebs-http4s-stir` | Path/query parameter codecs for http4s-stir |

### Other

| Module | What you get |
|---|---|
| `kebs-scalacheck` | `Arbitrary` instances with minimal/normal/maximal generators |
| `kebs-jsonschema` | JSON Schema generation (Scala 2 only) |
| `kebs-pureconfig` | PureConfig `ConfigReader` / `ConfigWriter` |
| `kebs-baklava` | Baklava schema support |
| `kebs-instances` | Pre-built `InstanceConverter`s for `java.time`, `UUID`, `URI`, etc. |
