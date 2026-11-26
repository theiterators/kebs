---
sidebar_position: 11.5
title: Jsoniter
---

# kebs-jsoniter

Automatic `JsonValueCodec` derivation for [jsoniter-scala](https://github.com/plokhotnyuk/jsoniter-scala), on JVM, Scala.js and Scala Native. Since kebs 3.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-jsoniter" % kebsVersion
```

## Basic usage

```scala
import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.jsoniter.KebsJsoniter

object ThingProtocol extends KebsJsoniter with CaseClass1ToValueClass
import ThingProtocol._

final case class ThingId(value: String)
final case class Thing(id: ThingId, tags: List[String], size: Option[Int])

writeToString(Thing(ThingId("x"), List("a"), None)) // {"id":"x","tags":["a"],"size":null}
readFromString[List[Thing]](json)
```

Or use the package object directly:

```scala
import pl.iterators.kebs.jsoniter._
```

There is an implicit codec of:

- case classes and sealed hierarchies, recursive ones too
- value classes, when `CaseClass1ToValueClass` is mixed in, and [opaque types](opaque.md)
- types with an `InstanceConverter` (see [instances](instances.md))
- collections, arrays and maps

A codec of your own (in a companion object, or in scope) always wins, for fields and elements too.

An implicit codec is created where it is used. Keep the ones you use often as values:

```scala
object Thing {
  implicit val codec: JsonValueCodec[Thing] = ThingProtocol.deriveCodec[Thing]
}
```

`deriveCodec` takes the configuration of its protocol, but value classes and converters are looked up in scope, so import the protocol that has
them.

There is no implicit codec of a `String`, a number, an `Option` or a tuple on its own, so that other libraries keep handling them in their own
way. Derive these explicitly:

```scala
implicit val codec: JsonValueCodec[Option[Thing]] = deriveCodec[Option[Thing]]
```

## JSON format

The JSON is that of [kebs-circe](circe.md):

- `None` is written as `null`; a missing key or a `null` is read as `None`.
- Tuples are arrays.
- The cases of a sealed hierarchy are wrapped: `{"Circle":{"r":1.0}}`, `{"Empty":{}}`.
- `java.time` types and `UUID` are written as jsoniter-scala writes them, unless there is an `InstanceConverter`.

Reading is stricter than with jsoniter-scala's own macro: a missing collection, a `null` that is not an `Option`'s, and a duplicate key are
errors. Unknown keys are skipped. Maps may have any number of entries.

Case objects and enum values are derived only as cases of their sealed hierarchy, so `writeToString(Color.Red)` needs a type:
`writeToString[Color](Color.Red)`.

`Either` and `BitSet` are not supported.

## Configuration

```scala
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.Naming

object ThingProtocol extends KebsJsoniter {
  override def kebsJsonConfig: JsonConfig =
    JsonConfig.default
      .withFieldNaming(Naming.kebabCase)
      .withConstructorNaming(Naming.snakeCase)
      .withNoneHandling(NoneHandling.Omit)
      .withDiscriminator("type") // {"type":"circle","r":1.0}; read from any position
      .withDefaults              // a missing key takes the field's default value
}
```

Snakified and capitalized field names have presets:

```scala
import pl.iterators.kebs.jsoniter.snakified._
import pl.iterators.kebs.jsoniter.capitalized._
```

Single fields can be renamed or left out:

```scala
import pl.iterators.kebs.jsoniter.annotations._

final case class User(@fieldName("user_name") name: String, @transientField cache: List[String] = Nil)
```

## Enum support

```scala
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterValueEnums}

object ThingProtocol extends KebsJsoniter with KebsEnumeratum with KebsJsoniterEnums with KebsJsoniterValueEnums
```

`KebsJsoniterEnumsUppercase` and `KebsJsoniterEnumsLowercase` change the case of the names.

## pekko-http

With [pekko-http-json](https://github.com/pjfanning/pekko-http-json), the same on Scala 2 and 3:

```scala
import com.github.pjfanning.pekkohttpjsoniterscala.JsoniterScalaSupport
import pl.iterators.kebs.jsoniter.KebsJsoniter

object ThingProtocol extends JsoniterScalaSupport with KebsJsoniter
import ThingProtocol._

complete(thing)
complete(StatusCodes.Created -> List(thing))
entity(as[Thing]) { thing => ... }
```

Strings, `Done`, entities, form data, status codes and empty options are still sent as pekko-http sends them.

## Next to jsoniter-scala's macro

A codec made by `JsonCodecMaker` is used like any other of your own. Where `JsonCodecMaker` itself runs with a kebs protocol in scope, it uses
kebs' codecs of the nested types, with kebs' configuration. Define them as values: Scala 2 needs that, and Scala 3 would create them anew on each
use.

```scala
implicit val thing: JsonValueCodec[Thing]   = deriveCodec[Thing]
implicit val holder: JsonValueCodec[Holder] = JsonCodecMaker.make
```
