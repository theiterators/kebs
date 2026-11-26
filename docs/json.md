---
sidebar_position: 8.5
title: JSON derivation
---

# JSON derivation

[kebs-circe](circe.md), [kebs-spray-json](spray-json.md), [kebs-play-json](play-json.md) and [kebs-jsoniter](jsoniter.md) share one derivation. It works the same on Scala 2.13 and 3, and the four modules write the same JSON, up to the conventions of each library (listed [below](#differences-between-the-modules)).

## What is derived

- **Case classes**, generic ones, recursive ones and those with more than 22 fields included.
- **Sealed hierarchies**, as an object with the name of the case: `{"Rect":{"w":1.0,"h":2.0}}`, `{"Empty":{}}`. Nested hierarchies are flattened to their leaves.
- **Value classes** (single-field case classes), as the wrapped value, when [marked](core-concepts.md#valueclasslikevc-f1) with `derives ValueClass` or `CaseClass1ToValueClass` is mixed in; [opaque](opaque.md) and [tagged](tagged.md) types too. That includes a single-field case of a sealed hierarchy: `{"Dot":2.0}`. With a discriminator, writing such a case fails.
- **Types with an `InstanceConverter`**, as the converted value (see [instances](instances.md)).
- **Enums and value enums**, as their name or value, when both the module's enum traits and the support of the enum (`KebsEnum` / `KebsValueEnum`, `KebsEnumeratum` / `KebsValueEnumeratum`, see [enums](enums.md)) are mixed in. Without that support an enum is a sealed hierarchy: `{"Red":{}}`.

Recursive types need nothing extra:

```scala
case class Node(value: Int, children: List[Node])
```

## Your own instances

An instance that exists is used instead of a derived one. One in scope (imported, mixed in or defined by you) always wins, also a generic one of your own over derived types. One in a companion object, or of the library, wins over a derived object. In circe, spray-json and play-json it does not win over the protocol's codec of a value class, of a type with an `InstanceConverter` or of an enum (in jsoniter: of an enum); put it in scope to use it there.

```scala
case class Page[A](items: List[A], next: Option[String])
object Page {
  // used for Page[Thing], also where Thing itself is derived or recursive
  implicit def encoder[A: Encoder]: Encoder[Page[A]] = ...
}
```

## Explicit derivation

Every protocol can also derive an instance where you ask for it: `deriveEncoder`, `deriveDecoder` and `deriveCodec` in circe, `deriveFormat` in spray-json, `deriveReads`, `deriveWrites` and `deriveFormat` in play-json, `deriveCodec` in jsoniter. They always derive the type itself, with the configuration of their protocol, also when it has an instance; types inside it that have none are derived within the same instance.

To derive nothing implicitly, use the semi-automatic protocol of the module: `KebsCirceSemiauto`, `KebsSprayJsonSemiauto`, `KebsPlayJsonSemiauto`, `KebsJsoniterSemiauto` (or import `circe.semiauto._` and so on). It has everything of the full protocol (value classes, converters, configuration, the methods above) except the implicit derivation, so every instance is made once, where you define it:

```scala
object Protocol extends KebsCirceSemiauto
import Protocol._

implicit val thingCodec: Codec.AsObject[Thing] = deriveCodec[Thing]
```

## Configuration

Override `kebsJsonConfig` in the protocol object:

```scala
import pl.iterators.kebs.circe.KebsCirce
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
| `Either` | `{"Right":{"value":1}}` | the bare value | not supported | not supported |
| a case object at its own type | `{}` | `{}` | not derived | not derived |
| explicit derivation | `deriveEncoder`, `deriveDecoder`, `deriveCodec` | `deriveFormat` | `deriveReads`, `deriveWrites`, `deriveFormat` | `deriveCodec` |
