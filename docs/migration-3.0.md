---
sidebar_position: 1.5
title: Migrating to kebs 3
---

# Migrating to kebs 3

Kebs 3 derives everything with one implementation for Scala 2.13 and 3. Most code compiles unchanged, but **some JSON changes**, and some of those changes do not show at compile time. Read the next section before you upgrade a service that stores JSON or exchanges it with others.

## JSON that changes

**circe, Scala 3: `derives` on recursive types.** Kebs 2 ignored such an instance and derived the type with the protocol's naming. Kebs 3 uses it, and it knows nothing of the protocol: with `snakified`, `foo_bar` becomes `fooBar`. The kebs 2 docs advised to add `derives` there. Remove it; kebs 3 derives recursive types without it.

**circe, Scala 3: an implicit `io.circe.derivation.Configuration`** is not read any more. One that you define, in the protocol or nearer than it, compiles and is ignored, so its naming is lost. Move it to [`kebsJsonConfig`](json.md#configuration). With `withDefaults`, a `null` for a field that is not an `Option` took the default; it is now an error.

**Existing instances win over derived ones.** In kebs 2, derivation was in scope wherever a protocol was imported and won over them in these places: everywhere on Scala 3, and on Scala 2 in circe's `snakified` / `capitalized` protocols and in spray-json. JSON written by kebs 2 in these shapes is not read by kebs 3.

| | kebs 2 | kebs 3 |
|---|---|---|
| circe: tuple field | `{"_1":3,"_2":"a"}` | `[3,"a"]` |
| circe: cats `NonEmptyList` field | `{"head":1,"tail":[2]}` | `[1,2]` |
| circe: field of type `Some[A]` | `{"value":5}` | `5` |
| circe, spray-json: type with an instance in its companion (not a value class, a type with a converter or an enum, see [JSON derivation](json.md#your-own-instances)) | derived | the companion's instance |
| spray-json, circe's `snakified` / `capitalized`, Scala 2: case class with an `InstanceConverter` | derived object | the converted value |

To keep the old shape, define an instance that writes it.

**circe, Scala 2: recursive types in containers.** For a recursive type in a generic type with a codec of its own (`List[Page[Rec]]`, `NonEmptyList[Rec]` and so on), kebs 2 used that codec outside and a derived object inside, in one document. Kebs 3 uses the codec at every level.

**spray-json and play-json, and circe on Scala 3: an enum without its support in scope** is written as `{"Red":{}}`. It did not compile before. It needs both the module's enum traits and `KebsEnum` / `KebsValueEnum` or `KebsEnumeratum` / `KebsValueEnumeratum`.

**circe, Scala 3, snakified:** a dash in a field name becomes an underscore (`foo-bar` is `foo_bar`), as on Scala 2.

**spray-json, Scala 2, more than 22 fields:** kebs 2 left out every field written as `null`, kebs 3 only `None`s.

**play-json without `CaseClass1ToValueClass`:** a single-field case class is now derived as an object (`{"value":"x"}`). It did not compile before, so this shows up as JSON, not as an error.

**A single-field case of a sealed hierarchy** is written as `{"Dot":2.0}` with `CaseClass1ToValueClass` mixed in. With a discriminator, writing it fails.

## Requirements

- Scala 2.13, or Scala **3.9** or newer
- **JDK 17** or newer

Kebs-core brings [hearth](https://github.com/kubuszok/hearth) (and, on Scala 2, `scala-compiler`).

Kebs 2 remains for older versions, on the `series/2.x` branch.

## Modules

- **kebs-akka-http is gone.** Use [kebs-pekko-http](pekko-http.md), or stay on kebs 2.
- **kebs-doobie** needs doobie 1.0.0-RC14, whose organization is `org.typelevel` and package `org.typelevel.doobie`.
- **kebs-circe** no longer brings circe-generic and circe-generic-extras. Add them if you use `semiauto` or `@JsonCodec`.
- **kebs-jsoniter** is [new](jsoniter.md).

## Behaviour

- **pekko-http, http4s-stir:** a path segment that cannot be converted (an unknown enum value, a rejected value class) does not match. It was a 500, it is now a 404 if no other route matches.
- **pekko-http with spray-json, play-json, or circe on Scala 3:** your own sealed hierarchies now have an instance, so they are sent as JSON, also if their companion has a `Marshaller`.
- **Scala 3:** value classes and derived case classes are built with the companion `apply`, so one that validates is honoured when reading, as on Scala 2. Its exception is a decoding error for a value class in circe and play-json; for other case classes it is thrown as it is.
- **Enums are read case-insensitively** by doobie, slick, baklava's `ToHeader` and the `uppercase` / `lowercase` presets too. An exact match wins. Set an `EnumNaming` with `caseInsensitive = false` to turn this off (the presets do not read it).
- **Enumeratum, Scala 3:** the entries are those of `values`, also when it leaves some out.
- **spray-json, Scala 3:** a sealed hierarchy without an enum format is written as `{"Red":{}}`. It did not compile before.
- **spray-json, Scala 3:** a missing field that is not an `Option` fails as "missing required member" (it was read as `null`).
- **circe:** an unknown case of a sealed hierarchy is reported as "Unknown case 'X', expected one of: ...". The `snakified` / `capitalized` protocols on Scala 2 decode a case object from any object. On Scala 3 the messages are those of circe-generic, they were those of circe's own derivation.
- **scalacheck:** an enum whose support is in scope is generated from its `values`.
- **pureconfig:** a type with an `InstanceConverter`, and enums and value enums with their support in scope, are read and written by kebs, also if PureConfig or the companion has an instance.

## Source changes

### circe

- **Scala 3:** replace `override implicit val configuration: Configuration` with [`kebsJsonConfig`](json.md#configuration). There is no `withStrictDecoding`. `Naming.kebabCase` also turns `_` into `-`, circe's does not.
- `derives` on recursive types is not needed any more. Remove it, see above.
- Removed with it: `exportEncoder`, `exportDecoder` and the implicit `configuration` (Scala 3), `genericSnakifiedEncoder` and its relatives (Scala 2).
- **Scala 2:** "case-class-like" classes (`class X(val x: Int)`) are not derived. Import one protocol per scope: two cancel each other's implicits.

### spray-json

- **`jsonFormatN` is not implicit any more.** A selective import of it must import `kebsAutoJsonFormat` (and `kebsAutoJsonFormatSealed` for sealed hierarchies).
- `jsonFormatRec` is deprecated: use `jsonFormatN`, or nothing.
- Removed: `FieldNamingStrategy`, `namingStrategy`, `nullOptions` (Scala 3), `snakifiedJsonFormatN`, `capitalizedJsonFormatN`, `_kebs_getField` (Scala 2). Override [`kebsJsonConfig`](json.md#configuration), or mix in `NullOptions`.
- To hide derivation, hide `kebsAutoJsonFormat` and `kebsAutoJsonFormatSealed`. Hiding `jsonFormatN` has no effect any more.

### play-json

- The existing import now derives `Reads` and `OWrites`. Formats you have still win, so nothing has to change. `Json.format` of a case class with several fields can go, unless it uses a `JsonConfiguration` of its own.
- **Scala 2:** import one protocol per scope.

### Enums

- The implicit methods of the enum traits have an `EnumNaming` parameter. This matters only where they are called or overridden explicitly: `enumDecoderImpl`, `enumEncoderImpl` (circe, play-json), `jsonEnumFormat` (spray-json), `enumMeta` and its array variants (doobie), `enumValueColumn`, its list variants and `enumToFromStringForHstore` (slick), `kebsEnumUnmarshaller`, `asEnum` (pekko-http, http4s-stir), `EnumString`, `enumQueryParamDecoder` (http4s), `toQueryParamEnum`, `toPathParamEnum`, `toHeaderEnum` and `enumLikeSchema` (baklava).
- Mixing an enum trait with its `Uppercase` / `Lowercase` variant does not compile.

### All JSON modules: rare compile errors

- a generic codec of your own that needs an instance a type does not have, such as a `RootJsonFormat` or an `Encoder.AsObject` of a type with only a `JsonFormat` or `Encoder` (kebs 2 ignored the codec)
- in circe, spray-json and play-json, a value class that contains itself; in all of them, types that contain ever larger applications of themselves
- on Scala 2, a case class with an implicit parameter list

## New in kebs 3

- [JSON derivation](json.md) on both Scala versions in all four modules: sealed hierarchies, recursive types, naming, discriminators, default values, `@fieldName`, `@transientField`
- [naming styles](enums.md#naming-styles) for enums, in every module
- `AnyVal` case classes on Scala 3 (kebs 2 did not compile them), written as the wrapped value with `CaseClass1ToValueClass`
- `scala.Enumeration` on Scala 3, `KebsValueEnum` on Scala 2
- value enums in http4s, enums and converters in pureconfig, enums in scalacheck
