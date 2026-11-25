---
sidebar_position: 8.5
title: JSON derivation
---

# JSON derivation

[kebs-circe](circe.md), [kebs-spray-json](spray-json.md), [kebs-play-json](play-json.md) and [kebs-jsoniter](jsoniter.md) share one derivation. It works the same on Scala 2.13 and 3, and the four modules write the same JSON, up to the conventions of each library (listed below).

## What is derived

- **Case classes**, generic ones, recursive ones and those with more than 22 fields included.
- **Sealed hierarchies**, as an object with the name of the case: `{"Rect":{"w":1.0,"h":2.0}}`, `{"Empty":{}}`. Nested hierarchies are flattened to their leaves.
- **Value classes** (single-field case classes), as the wrapped value, when `CaseClass1ToValueClass` is mixed in; [opaque](opaque.md) and [tagged](tagged.md) types too.
- **Types with an `InstanceConverter`**, as the converted value (see [instances](instances.md)).
- **Enums and value enums**, as their name or value, when the module's enum traits are mixed in (see [enums](enums.md)).

Recursive types need nothing extra:

```scala
case class Node(value: Int, children: List[Node])
```

## Your own instances always win

An instance that exists is used instead of a derived one: one in scope, one in a companion object, one of the library, or a generic one of your own over derived types.

```scala
case class Page[A](items: List[A], next: Option[String])
object Page {
  // used for Page[Thing], also where Thing itself is derived or recursive
  implicit def encoder[A: Encoder]: Encoder[Page[A]] = ...
}
```

## Configuration

Override `kebsJsonConfig` in the protocol object:

```scala
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.Naming

object ThingProtocol extends KebsCirce {
  override def kebsJsonConfig: JsonConfig =
    super.kebsJsonConfig
      .withFieldNaming(Naming.kebabCase)
      .withConstructorNaming(Naming.snakeCase)
      .withNoneHandling(NoneHandling.Omit)
      .withDiscriminator("type")
      .withDefaults
}
```

| Setting | Default | Effect |
|---|---|---|
| `withFieldNaming` | names as they are | renames fields |
| `withConstructorNaming` | names as they are | renames the cases of sealed hierarchies |
| `withNoneHandling` | `WriteNull` (circe, jsoniter), `Omit` (spray-json, play-json) | `None` is written as `null`, or left out |
| `withDiscriminator` | none | `{"type":"Rect","w":1.0,"h":2.0}` instead of the wrapper object |
| `withDefaults` | off | a missing key takes the default value of the field |

Start from `super.kebsJsonConfig`: `JsonConfig.default` writes `null`s, in spray-json and play-json too.

`Naming` has `identity`, `snakeCase`, `screamingSnakeCase`, `kebabCase`, `camelCase`, `pascalCase`, `lowercase`, `uppercase` and `custom(f)`.

The presets `snakified` and `capitalized` of each module set the field naming.

## Annotations

Each module has `fieldName` and `transientField` in its `annotations` package:

```scala
import pl.iterators.kebs.circe.annotations._

case class User(@fieldName("user_name") name: String, @transientField cache: List[String] = Nil)
```

A `@transientField` is neither written nor read, and needs a default value.

## Differences between the modules

| | circe | spray-json | play-json | jsoniter |
|---|---|---|---|---|
| `None` by default | `null` | left out | left out | `null` |
| order of members | declaration | by name | declaration | declaration |
| a discriminator is written | last | by name | last | first |
| explicit derivation | | `jsonFormatN`, `jsonFormatSealed` | `deriveReads`, `deriveWrites`, `deriveFormat` | `deriveCodec` |
