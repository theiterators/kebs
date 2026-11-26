## Kebs

##### Scala library to eliminate boilerplate

[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)](https://central.sonatype.com/artifact/pl.iterators/kebs-core_2.13)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://github.com/theiterators/kebs/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/theiterators/kebs/actions/workflows/ci.yml?query=branch%3Amaster)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com). Kebs 3 supports Scala 2.13 and Scala 3.9 or newer, on JDK 17 or newer. Kebs 2 is on the [`series/2.x`](https://github.com/theiterators/kebs/tree/series/2.x) branch.

### What is Kebs?

Kebs automatically derives typeclass instances (JSON codecs, DB column mappings, HTTP unmarshallers, etc.) for your domain types so you don't have to write them by hand.

```scala
case class UserId(value: String) extends AnyVal
case class Email(value: String) extends AnyVal

// Without Kebs — manual boilerplate for every type × every library:
implicit val userIdCol: BaseColumnType[UserId] = MappedColumnType.base(_.value, UserId.apply)
implicit val userIdEnc: Encoder[UserId] = Encoder[String].contramap(_.value)
// ... repeat dozens of times

// With Kebs — just mix in a trait. Everything is derived at compile time.
```

### Documentation

**[https://theiterators.github.io/kebs/](https://theiterators.github.io/kebs/)**

### Quick start

```scala
// build.sbt — pick the modules you need; the latest version is in the badge above
libraryDependencies ++= Seq(
  "pl.iterators" %% "kebs-slick"      % kebsVersion,
  "pl.iterators" %% "kebs-circe"      % kebsVersion,
  "pl.iterators" %% "kebs-pekko-http" % kebsVersion,
  "pl.iterators" %% "kebs-instances"  % kebsVersion
)
```

### Available modules

| Module | What you get |
|---|---|
| `kebs-slick` | Automatic Slick column type mappings, Postgres array & hstore support |
| `kebs-doobie` | Doobie `Meta` / `Get` / `Put` instances |
| `kebs-spray-json` | Spray `JsonFormat` derivation |
| `kebs-play-json` | Play JSON `Reads` / `OWrites` derivation |
| `kebs-circe` | Circe `Encoder` / `Decoder` derivation |
| `kebs-jsoniter` | jsoniter-scala `JsonValueCodec` derivation (since 3.0) |
| `kebs-pekko-http` | Pekko HTTP unmarshallers + path matchers |
| `kebs-http4s` | http4s path/query parameter codecs |
| `kebs-http4s-stir` | http4s-stir directives + path matchers |
| `kebs-scalacheck` | `Arbitrary` instance generation |
| `kebs-pureconfig` | PureConfig `ConfigReader` / `ConfigWriter` |
| `kebs-jsonschema` | JSON Schema generation (Scala 2 only) |
| `kebs-baklava` | Baklava schema + parameter support |
| `kebs-tagged` | Tagged types for Scala 2 |
| `kebs-tagged-meta` | `@tagged` code generation (Scala 2 only) |
| `kebs-opaque` | Opaque type support for Scala 3 |
| `kebs-enum` | Scala 3 native enums + `scala.Enumeration` |
| `kebs-enumeratum` | Enumeratum support (Scala 2 & 3) |
| `kebs-instances` | Pre-built converters for `java.time`, `UUID`, `URI`, etc. |

### Migrating from kebs 2

See the [migration guide](docs/migration-3.0.md). Some JSON changes, so read it before you upgrade a service that has stored JSON.

### IntelliJ support

[Kebs for IntelliJ](https://plugins.jetbrains.com/plugin/16069-kebs) plugin adds support for `@tagged` generated code.
