---
sidebar_position: 12
title: Jsoniter
---

# kebs-jsoniter

Automatic `JsonValueCodec` derivation for [jsoniter-scala](https://github.com/plokhotnyuk/jsoniter-scala).

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-jsoniter" % kebsVersion
```

## Basic usage

### Scala 3 — explicit `deriveCodec`

On Scala 3, wire types declare their codec once, in the companion object:

```scala
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.jsoniter.KebsJsoniter

object ThingProtocol extends KebsJsoniter
import ThingProtocol._

final case class Thing(id: ThingId, name: ThingName)
object Thing {
  implicit val codec: JsonValueCodec[Thing]         = deriveCodec
  implicit val seqCodec: JsonValueCodec[Seq[Thing]] = deriveCodec // jsoniter codecs are whole-value:
                                                                  // derive the exact wire shape
}
```

`deriveCodec` understands kebs value classes, opaque types and `InstanceConverter`-backed types in
fields — they are serialized flat (as their underlying representation). Companion codecs are found
through pekko-http's marshaller search with zero extra ceremony: `complete(OK -> thing)` and
`entity(as[Thing])` just work.

Why not automatic like on Scala 2? pekko-http's `Marshaller[-A, +B]` is contravariant in `A`; with
a universal implicit codec in scope, Scala 3 maximizes the unpinned type variable to `Any` and
codec derivation aborts — breaking **every** marshalling site in that scope, including plain
`String`s. Scala 3 therefore makes derivation explicit (a call, not an implicit), while codecs for
kebs value classes / opaque types themselves are still summoned implicitly when a codec for the
underlying type is in scope.

If you want automatic marshalling back, see
[kebs-jsoniter-pekko-http](#automatic-pekko-http-marshalling-scala-3-opt-in) below.

### Scala 2 — automatic derivation

On Scala 2 the classic mix-in contract is unchanged — every codec is derived implicitly:

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniter

object ThingProtocol extends KebsJsoniter
```

`implicit val codec: JsonValueCodec[Thing] = deriveCodec` also compiles on Scala 2 (an implicit
macro is explicitly callable), so cross-built sources can use the Scala 3 style everywhere.

This derives `JsonValueCodec` for:
- 1-element case classes (flat format)
- Multi-field case classes
- Case classes with > 22 fields
- Types with an `InstanceConverter` (e.g. `UUID`, `java.time` types — see [instances](instances.md))

## Snakified / capitalized field names

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniterSnakified

object ThingProtocol extends KebsJsoniterSnakified

// Scala 3: derive with the flavor's deriveCodec
import ThingProtocol._
object Thing { implicit val codec: JsonValueCodec[Thing] = deriveCodec } // {"some_field": ...}

// or via package object:
import pl.iterators.kebs.jsoniter.snakified._
```

Capitalized variant:

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniterCapitalized
// or: import pl.iterators.kebs.jsoniter.capitalized._
```

## Enum support

```scala
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterValueEnums}

object ThingProtocol extends KebsJsoniter with KebsJsoniterEnums with KebsJsoniterValueEnums
```

Or via the enums package object:

```scala
import pl.iterators.kebs.jsoniter.enums._            // default casing + value enums
import pl.iterators.kebs.jsoniter.enums.uppercase._   // UPPERCASE + value enums
import pl.iterators.kebs.jsoniter.enums.lowercase._   // lowercase + value enums
```

Enum codecs are case-insensitive on read by default and preserve the original name on write.

## Instance support (java.time, UUID, etc.)

Mix in `kebs-instances` traits to get automatic codecs for common types:

```scala
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.UtilInstances

object ThingProtocol extends KebsJsoniter with TimeInstances with UtilInstances
```

## Automatic pekko-http marshalling (Scala 3, opt-in)

`kebs-jsoniter-pekko-http` restores the "mix in one trait and everything marshals" ergonomics on
Scala 3:

```scala
libraryDependencies += "pl.iterators" %% "kebs-jsoniter-pekko-http" % kebsVersion
```

```scala
import com.github.pjfanning.pekkohttpjsoniterscala.JsoniterScalaSupport
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.pekkohttp.AutoJsonMarshalling

object protocol extends JsoniterScalaSupport with KebsJsoniter with AutoJsonMarshalling
```

Types with a companion codec keep using it; everything else is derived on the fly. **Sharp edges
(all deliberate, all demonstrated in the module's tests):**

- only entity, status+value, and entity-unmarshaller shapes are covered; other pekko shapes
  (status+headers+value triples, streaming `Source`s, ...) fail to compile with a `scala.Any` error;
- types with existing non-JSON marshallers get hijacked: `complete(OK -> "hello")` becomes
  `application/json`;
- types jsoniter cannot derive stop compiling even though pekko could marshal them
  (e.g. `complete(OK -> HttpEntity("raw"))`).

