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

## http4s usage (Scala 3)

```scala
import pl.iterators.kebs.http4s.{given, *}
import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.enums.KebsEnum
import pl.iterators.kebs.instances.KebsInstances._

opaque type Age = Int
object Age extends Opaque[Age, Int]

case class UserId(id: UUID)

enum Color { case Red, Blue, Green }

object AgeQueryParamMatcher extends QueryParamDecoderMatcher[Age]("age")
object ColorQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Color]("color")

val routes = HttpRoutes.of[IO] {
  case GET -> Root / "user" / WrappedUUID[UserId](userId)        => ...
  case GET -> Root / "age" / WrappedInt[Age](age)                => ...
  case GET -> Root / "color" / EnumString[Color](color)          => ...
  case GET -> Root / "currency" / InstanceString[Currency](curr) => ...
  case GET -> Root / "query" :? AgeQueryParamMatcher(age)        => ...
}
```

Path segment matchers available: `WrappedInt`, `WrappedLong`, `WrappedString`, `WrappedUUID`, `InstanceString`, `EnumString`.

## http4s-stir usage

http4s-stir provides Akka HTTP-style directives for http4s:

```scala
import pl.iterators.kebs.http4sstir.unmarshallers.KebsHttp4sStirUnmarshallers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.instances.net.URIString
```
