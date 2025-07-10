---
sidebar_position: 1
title: Introduction to Kebs
---

# Kebs

##### Scala library that encourages better domain modeling by eliminating primitive obsession

[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)](https://central.sonatype.com/artifact/pl.iterators/kebs-core_2.13/2.1.2)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://travis-ci.org/theiterators/kebs.svg?branch=master)](https://github.com/theiterators/kebs/actions/workflows/ci.yml?query=branch%3Amaster)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com).

## What is Kebs?

**Kebs** is a Scala library that encourages better domain modeling by making it effortless to use strong types instead of primitives. The name "Kebs" comes from the Polish word "kebab" - just like a kebab brings together different ingredients, Kebs brings together your domain types with popular Scala libraries seamlessly.

The core philosophy of Kebs is simple: **you should be able to use proper domain types without paying the integration cost**. Instead of using primitive types like `String`, `Int`, or `UUID` directly in your domain, you can wrap them in meaningful types and Kebs will automatically handle all the necessary conversions for databases, JSON serialization, HTTP parameters, and more.

Kebs supports Scala 2 and 3, and works with popular libraries like Slick, Circe, Play JSON, Akka HTTP, Doobie, and more. It eliminates the boilerplate code required to integrate your domain types with these libraries, allowing you to focus on building your application logic.

## Supported Type Patterns

Kebs supports multiple patterns for creating strong domain types:

### 1. Value Classes (Scala 2 and 3)

Zero runtime overhead with maximum type safety:

```scala
case class UserId(value: String) extends AnyVal
case class Price(amount: BigDecimal) extends AnyVal
case class Quantity(count: Int) extends AnyVal
```

### 2. Tagged Types (Scala 2)

Compile-time only type distinctions:

```scala
import pl.iterators.kebs.tagged._

trait UserIdTag
type UserId = String @@ UserIdTag

val userId: UserId = "user123".taggedWith[UserIdTag]
```

### 3. Opaque Types (Scala 3)

Zero-cost abstractions with encapsulation:

```scala
import pl.iterators.kebs.opaque._

opaque type UserId = String
object UserId extends Opaque[UserId, String] {
  override def validate(value: String): Either[String, UserId] =
    if (value.nonEmpty) Right(value) else Left("UserId cannot be empty")
}

val userId = UserId("user123") // Validated construction
val rawValue: String = userId.unwrap
```

### 4. Enumerations (Scala 2 and 3)

Both Scala's native enums and Enumeratum:

```scala
// Scala 3 enum
enum Status {
  case Active, Inactive, Pending
}

// Enumeratum
sealed trait Priority extends EnumEntry
object Priority extends Enum[Priority] {
  case object High extends Priority
  case object Medium extends Priority
  case object Low extends Priority
  val values = findValues
}
```

## Fighting Primitive Obsession

[Primitive Obsession](https://refactoring.guru/smells/primitive-obsession) is a code smell where you use primitive data types to represent domain concepts. For example:

```scala
// Primitive obsession - what do these strings represent?
case class User(id: String, email: String, name: String)

def findUser(userId: String): User = ???
def sendEmail(email: String, subject: String): Unit = ???

// Easy to make mistakes:
val user = User("john@example.com", "user-123", "John Doe") // Wrong order!
sendEmail("user-123", "Welcome!") // Wrong parameter!
```

Kebs encourages you to create meaningful domain types:

```scala
case class UserId(value: String) extends AnyVal
case class EmailAddress(value: String) extends AnyVal
case class UserName(value: String) extends AnyVal

case class User(id: UserId, email: EmailAddress, name: UserName)

def findUser(userId: UserId): User = ???
def sendEmail(email: EmailAddress, subject: String): Unit = ???

// Now the compiler prevents mistakes:
val user = User(UserId("user-123"), EmailAddress("john@example.com"), UserName("John Doe"))
sendEmail(user.email, "Welcome!") // Type-safe!
```

## The Integration Problem

The main barrier to using strong domain types has always been the integration cost. Without Kebs, each wrapper type requires manual boilerplate for every library you use:

```scala
case class UserId(value: String) extends AnyVal

// Slick database mapping
implicit val userIdColumnType: BaseColumnType[UserId] =
  MappedColumnType.base(_.value, UserId.apply)

// JSON serialization
implicit val userIdFormat: Format[UserId] =
  Format(Reads.of[String].map(UserId.apply), Writes.of[String].contramap(_.value))

// HTTP parameter parsing
implicit val userIdUnmarshaller: Unmarshaller[String, UserId] =
  Unmarshaller.strict(UserId.apply)

// And so on for every type and every library...
```

**Kebs eliminates this integration cost entirely**, making strong domain types as easy to use as primitives.

## Library Integrations

Once you define your domain types, Kebs automatically provides integration with:

### Database Libraries

- **[`kebs-slick`](https://github.com/theiterators/kebs/tree/master/slick/)**: Automatic column type mappings for Slick
- **[`kebs-doobie`](https://github.com/theiterators/kebs/tree/master/doobie/)**: Meta instances for Doobie

### JSON Libraries

- **[`kebs-circe`](https://github.com/theiterators/kebs/tree/master/circe/)**: Automatic encoder/decoder derivation for Circe
- **[`kebs-play-json`](https://github.com/theiterators/kebs/tree/master/play-json/)**: Format instances for Play JSON
- **[`kebs-spray-json`](https://github.com/theiterators/kebs/tree/master/spray-json/)**: JsonFormat instances for Spray JSON

### HTTP Libraries

- **[`kebs-akka-http`](https://github.com/theiterators/kebs/tree/master/akka-http/)**: Unmarshaller instances for Akka HTTP
- **[`kebs-pekko-http`](https://github.com/theiterators/kebs/tree/master/pekko-http/)**: Unmarshaller instances for Pekko HTTP
- **[`kebs-http4s`](https://github.com/theiterators/kebs/tree/master/http4s/)**: Path and query parameter extractors for http4s
- **[`kebs-http4s-stir`](https://github.com/theiterators/kebs/tree/master/http4s-stir/)**: Path and query parameter extractors for http4s-stir

### Other Integrations

- **[`kebs-scalacheck`](https://github.com/theiterators/kebs/tree/master/scalacheck/)**: Automatic Arbitrary instance generation
- **[`kebs-jsonschema`](https://github.com/theiterators/kebs/tree/master/jsonschema/)**: JSON Schema generation
- **[`kebs-pureconfig`](https://github.com/theiterators/kebs/tree/master/pureconfig/)**: Configuration reading support
- **[`kebs-baklava`](https://github.com/theiterators/kebs/tree/master/baklava/)**: Baklava integration for type-safe parammeters handling

## Before and After Kebs

Here's a concrete example showing how Kebs transforms your development experience:

### Before Kebs: Primitive Obsession

```scala
// Using primitives - error-prone and unclear
case class Order(
  id: String,           // What kind of ID?
  customerId: String,   // Easy to mix up with order ID
  amount: BigDecimal,   // In what currency?
  quantity: Int,        // Of what?
  status: String        // What are valid values?
)

// Manual mappings for every library
object OrderMappings {
  // Slick mappings
  implicit val orderTable = TableQuery[Orders]

  // JSON formats
  implicit val orderFormat = Json.format[Order]

  // Validation logic scattered everywhere
  def validateOrderId(id: String): Boolean = id.startsWith("ORD-")
  def validateStatus(status: String): Boolean =
    Set("pending", "confirmed", "shipped").contains(status)
}
```

### After Kebs: Strong Domain Types

```scala
// Clear, type-safe domain model
case class OrderId(value: String) extends AnyVal
case class CustomerId(value: String) extends AnyVal
case class Amount(value: BigDecimal) extends AnyVal
case class Quantity(value: Int) extends AnyVal

enum OrderStatus {
  case Pending, Confirmed, Shipped
}

case class Order(
  id: OrderId,
  customerId: CustomerId,
  amount: Amount,
  quantity: Quantity,
  status: OrderStatus
)

// JSON serialization works automatically
// HTTP parameter parsing works automatically
// Everything just works!
```

## Key Benefits

1. **Encourages Better Design**: Makes it easy to use proper domain types instead of primitives
2. **Eliminates Integration Cost**: No boilerplate for database mappings, JSON codecs, etc.
3. **Prevents Runtime Errors**: Catch type mismatches at compile time
4. **Improves Code Readability**: Method signatures clearly express intent
5. **Zero Runtime Overhead**: All derivation happens at compile time
6. **Consistent Behavior**: Uniform handling across different libraries

## The Kebs Philosophy

Kebs is built on the belief that **good domain modeling should be effortless**. You shouldn't have to choose between type safety and productivity. With Kebs, you can:

- Model your domain with meaningful types
- Eliminate primitive obsession
- Catch errors at compile time
- Write self-documenting code
- Integrate seamlessly with the Scala ecosystem

Stop fighting with boilerplate and start building better software with strong, expressive types. Kebs handles the plumbing so you can focus on your domain!
