## Kebs

##### Scala library to eliminate boilerplate

[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)](https://central.sonatype.com/artifact/pl.iterators/kebs-core_2.13)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://github.com/theiterators/kebs/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/theiterators/kebs/actions/workflows/ci.yml?query=branch%3Amaster)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com). Supports Scala 2.13 and Scala 3.

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
// build.sbt — pick the modules you need
libraryDependencies ++= Seq(
  "pl.iterators" %% "kebs-slick"      % "2.1.6",
  "pl.iterators" %% "kebs-circe"      % "2.1.6",
  "pl.iterators" %% "kebs-pekko-http" % "2.1.6",
  "pl.iterators" %% "kebs-instances"  % "2.1.6"
)
```

### Available modules

| Module | What you get |
|---|---|
| `kebs-slick` | Automatic Slick column type mappings, Postgres array & hstore support |
| `kebs-doobie` | Doobie `Meta` / `Get` / `Put` instances |
| `kebs-spray-json` | Spray `JsonFormat` derivation (flat, snakified, >22 fields) |
| `kebs-play-json` | Play JSON `Format` instances |
| `kebs-circe` | Circe `Encoder` / `Decoder` derivation (snakified/capitalized) |
| `kebs-akka-http` | Akka HTTP unmarshallers + path matchers (Scala 2 only) |
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
| `kebs-enum` | Scala 3 native enums + Scala 2 `Enumeration` |
| `kebs-enumeratum` | Enumeratum support (Scala 2 & 3) |
| `kebs-instances` | Pre-built converters for `java.time`, `UUID`, `URI`, etc. |

### Kebs 2.0 migration guide

* Mix in `CaseClass1ToValueClass` if you use value classes.
* Extend value-enums with `ValueEnumLikeEntry[V]`.
* Use `KebsEnumeratum` / `KebsValueEnumeratum` for Enumeratum, `KebsEnum` / `KebsValueEnum` for native enums.
* See the [full documentation](https://theiterators.github.io/kebs/) for details.

### IntelliJ support

[Kebs for IntelliJ](https://plugins.jetbrains.com/plugin/16069-kebs) plugin adds support for `@tagged` generated code.
