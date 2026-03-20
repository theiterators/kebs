---
sidebar_position: 9
title: http4s
---

# kebs-http4s / kebs-http4s-stir

Path and query parameter codecs for http4s and http4s-stir.

## Setup

```scala
// http4s
libraryDependencies += "pl.iterators" %% "kebs-http4s" % kebsVersion

// http4s-stir (Akka HTTP-style directives for http4s)
libraryDependencies += "pl.iterators" %% "kebs-http4s-stir" % kebsVersion
```

Both modules also publish Scala.js artifacts.

## kebs-http4s

### Usage (Scala 3)

```scala
import pl.iterators.kebs.http4s.{given, *}
import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.enums.KebsEnum
import pl.iterators.kebs.instances.KebsInstances._

opaque type Age = Int
object Age extends Opaque[Age, Int]

case class UserId(id: UUID)

enum Color { case Red, Blue, Green }

val routes = HttpRoutes.of[IO] {
  case GET -> Root / "user" / WrappedUUID[UserId](userId)        => ...
  case GET -> Root / "age" / WrappedInt[Age](age)                => ...
  case GET -> Root / "color" / EnumString[Color](color)          => ...
  case GET -> Root / "currency" / InstanceString[Currency](curr) => ...
  case GET -> Root / "day" / InstanceInt[DayOfWeek](dow)         => ...
  case GET -> Root / "ts" / InstanceLong[Instant](ts)            => ...
}
```

### Path segment matchers

| Matcher | Extracts | Via |
|---|---|---|
| `WrappedInt[T]` | `Int` path segment → `T` | `ValueClassLike[T, Int]` |
| `WrappedLong[T]` | `Long` path segment → `T` | `ValueClassLike[T, Long]` |
| `WrappedString[T]` | `String` path segment → `T` | `ValueClassLike[T, String]` |
| `WrappedUUID[T]` | `UUID` path segment → `T` | `ValueClassLike[T, UUID]` |
| `InstanceString[T]` | `String` path segment → `T` | `InstanceConverter[T, String]` |
| `InstanceInt[T]` | `Int` path segment → `T` | `InstanceConverter[T, Int]` |
| `InstanceLong[T]` | `Long` path segment → `T` | `InstanceConverter[T, Long]` |
| `InstanceUUID[T]` | `UUID` path segment → `T` | `InstanceConverter[T, UUID]` |
| `EnumString[T]` | `String` path segment → `T` | `EnumLike[T]` |

### QueryParamDecoder derivation

The same import also provides automatic `QueryParamDecoder` instances for:
- Any type with a `ValueClassLike` (value classes, opaque types, tagged types)
- Any type with an `InstanceConverter` (java.time types, UUID, etc.)
- Any type with an `EnumLike`

```scala
object AgeQueryParamMatcher extends QueryParamDecoderMatcher[Age]("age")
object ColorQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Color]("color")
object OptionalYearMatcher extends OptionalQueryParamDecoderMatcher[Year]("year")

val routes = HttpRoutes.of[IO] {
  case GET -> Root / "query" :? AgeQueryParamMatcher(age) +& ColorQueryParamMatcher(color) => ...
}
```

### Scala 2

On Scala 2, use `KebsEnumeratum` instead of `KebsEnum`, and tagged types (`@@`) instead of opaque types. See the [Scala 2 tests](https://github.com/theiterators/kebs/blob/master/http4s/jvm/src/test/scala-2/pl/iterators/kebs/http4s/Http4sDslTests.scala) for examples.

## kebs-http4s-stir

http4s-stir provides Akka HTTP-style directives for http4s. Kebs integrates via two packages:

### Unmarshallers

```scala
import pl.iterators.kebs.http4sstir.unmarshallers.KebsHttp4sStirUnmarshallers
// or: import pl.iterators.kebs.http4sstir.unmarshallers._
```

Provides `FromStringUnmarshaller` for value classes, enums, value enums, and instance types. Works with `parameters`, `formFields`, and `entity` directives:

```scala
parameters("sortBy".as[Column], "order".as[SortOrder], "offset".as[Offset]) { ... }
formFields("yearMonth".as[YearMonth]) { ... }
```

### Path matchers

```scala
import pl.iterators.kebs.http4sstir.matchers.KebsHttp4sStirMatchers
// or: import pl.iterators.kebs.http4sstir.matchers._
```

Same extension methods as [Akka/Pekko HTTP matchers](akka-http.md#path-matchers): `.as[T]`, `.to[T]`, `.asEnum[T]`, `.asValueEnum[T]`, and chaining (`.to[URI].as[TaggedUri]`).
