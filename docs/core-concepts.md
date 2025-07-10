---
sidebar_position: 2
title: Core Concepts
---

# Core Concepts

Kebs is built around several fundamental typeclasses that enable automatic derivation of instances for various Scala libraries. Understanding these core abstractions will help you better understand how Kebs works and how to extend it for your own needs.

## ValueClassLike[VC, F1]

The [`ValueClassLike`](../core/src/main/scala-2/pl/iterators/kebs/core/macros/ValueClassReps.scala#L6) typeclass is the foundation of Kebs. It represents types that can be converted to and from a single underlying value, enabling automatic derivation for wrapper types.

### Definition

```scala
final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1)
```

### What it represents

`ValueClassLike[VC, F1]` captures the isomorphism between:

- `VC`: A wrapper type (like `UserId`, `EmailAddress`, etc.)
- `F1`: The underlying primitive type (like `String`, `Int`, etc.)

It provides two functions:

- `apply: F1 => VC` - Constructs the wrapper type from the underlying value
- `unapply: VC => F1` - Extracts the underlying value from the wrapper type

### Usage Examples

```scala
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass._

case class UserId(value: String) extends AnyVal
case class Age(years: Int) extends AnyVal

// ValueClassLike instances are automatically derived
val userIdLike = implicitly[ValueClassLike[UserId, String]]
val ageLike = implicitly[ValueClassLike[Age, Int]]

// Using the instances
val userId = userIdLike.apply("user123")  // UserId("user123")
val rawId = userIdLike.unapply(userId)    // "user123"

val age = ageLike.apply(25)               // Age(25)
val rawAge = ageLike.unapply(age)         // 25
```

### Integration with Libraries

`ValueClassLike` is used by all Kebs integration modules to automatically derive instances:

```scala
// Circe JSON codecs
implicit def encoder[VC, F1](implicit vcLike: ValueClassLike[VC, F1], enc: Encoder[F1]): Encoder[VC] =
  enc.contramap(vcLike.unapply)

// Slick column types
implicit def columnType[VC, F1](implicit vcLike: ValueClassLike[VC, F1], ct: BaseColumnType[F1]): BaseColumnType[VC] =
  MappedColumnType.base(vcLike.unapply, vcLike.apply)
```

## EnumLike[T]

The [`EnumLike`](../core/src/main/scala/pl/iterators/kebs/core/enums/EnumLike.scala#L5) typeclass provides a unified interface for working with enumeration types, supporting both name-based and ordinal-based operations.

### Definition

```scala
trait EnumLike[T] {
  def valuesToNamesMap: Map[T, String]
  // ... many derived methods
}
```

### What it represents

`EnumLike[T]` represents enumeration types where:

- `T`: The enum type (like `Color`, `Status`, etc.)
- Each enum value has an associated string name
- Values can be looked up by name (with various case sensitivity options)
- Values have ordinal positions

### Core Operations

#### Name-based Operations

```scala
def withName(name: String): T                        // Exact name match
def withNameOption(name: String): Option[T]          // Safe exact name match
def withNameIgnoreCase(name: String): T              // Case-insensitive match
def withNameUppercaseOnly(name: String): T           // Match uppercase name
def withNameLowercaseOnly(name: String): T           // Match lowercase name
```

#### Value Inspection

```scala
def values: immutable.Seq[T]                         // All enum values
def names: immutable.Seq[String]                     // All enum names
def getName(e: T): String                            // Get name for value
def valueOf(name: String): T                         // Alias for withName
```

#### Ordinal Operations

```scala
def fromOrdinal(ordinal: Int): T                     // Get value by position
def indexOf(member: T): Int                          // Get position of value
```

### Usage Examples

```scala
// Scala 3 enum
enum Color {
  case Red, Green, Blue
}

// With EnumLike instance (provided by kebs-enum module)
val colorEnum = implicitly[EnumLike[Color]]

// Name-based lookup
val red = colorEnum.withName("Red")                  // Color.Red
val blue = colorEnum.withNameOption("Blue")          // Some(Color.Blue)
val invalid = colorEnum.withNameOption("Yellow")     // None

// Case-insensitive lookup
val green = colorEnum.withNameIgnoreCase("green")    // Color.Green

// Ordinal operations
val firstColor = colorEnum.fromOrdinal(0)            // Color.Red
val blueIndex = colorEnum.indexOf(Color.Blue)        // 2

// Inspection
val allColors = colorEnum.values                     // Seq(Red, Green, Blue)
val allNames = colorEnum.names                       // Seq("Red", "Green", "Blue")
```

## ValueEnumLikeEntry[ValueType] and ValueEnumLike[ValueType, EntryType]

The [`ValueEnumLikeEntry`](../core/src/main/scala/pl/iterators/kebs/core/enums/ValueEnumLike.scala#L5) and [`ValueEnumLike`](../core/src/main/scala/pl/iterators/kebs/core/enums/ValueEnumLike.scala#L9) typeclasses support enumerations where each entry has an associated value of a specific type (like `Int`, `String`, etc.).

### Definitions

```scala
trait ValueEnumLikeEntry[ValueType] {
  def value: ValueType
}

trait ValueEnumLike[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]] {
  def values: immutable.Seq[EntryType]
  // ... derived methods
}
```

### What they represent

- `ValueEnumLikeEntry[ValueType]`: Represents a single enum entry that has an associated value
- `ValueEnumLike[ValueType, EntryType]`: Represents an enumeration where each entry has a value of type `ValueType`

This is useful for enums that need to serialize to specific values rather than their names.

### Core Operations

```scala
def withValue(value: ValueType): EntryType           // Find entry by value
def withValueOption(value: ValueType): Option[EntryType]  // Safe value lookup
def valueOf(value: ValueType): EntryType             // Alias for withValue
def valueOfOption(value: ValueType): Option[EntryType]    // Safe valueOf
```

### Usage Examples

```scala
// Scala 3 value enum
enum Priority(val value: Int) extends ValueEnumLikeEntry[Int] {
  case High extends Priority(1)
  case Medium extends Priority(2)
  case Low extends Priority(3)
}

// Enumeratum value enum (Scala 2 & 3)
sealed abstract class HttpStatus(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
object HttpStatus extends IntEnum[HttpStatus] {
  case object Ok extends HttpStatus(200)
  case object NotFound extends HttpStatus(404)
  case object InternalServerError extends HttpStatus(500)
  val values = findValues
}

// With ValueEnumLike instance
val priorityEnum = implicitly[ValueEnumLike[Int, Priority]]

// Value-based lookup
val high = priorityEnum.withValue(1)                 // Priority.High
val medium = priorityEnum.withValueOption(2)         // Some(Priority.Medium)
val invalid = priorityEnum.withValueOption(99)       // None

// Get value from entry
val highValue = Priority.High.value                  // 1
```

### Integration Benefits

Value enums are particularly useful for:

- Database storage (store `1` instead of `"High"`)
- API contracts (numeric codes)
- Performance (smaller serialized size)
- Backwards compatibility (can change names without breaking storage)

## InstanceConverter[Obj, Val]

The [`InstanceConverter`](../core/src/main/scala/pl/iterators/kebs/core/instances/InstanceConverter.scala#L6) typeclass provides bidirectional conversion between domain objects and their underlying representations, with built-in error handling.

### Definition

```scala
trait InstanceConverter[Obj, Val] {
  def encode(obj: Obj): Val
  def decode(value: Val): Obj
}
```

### What it represents

`InstanceConverter[Obj, Val]` represents a bidirectional conversion between:

- `Obj`: A domain object type (like `UUID`, `LocalDate`, `URI`, etc.)
- `Val`: A simpler representation type (like `String`, `Long`, etc.)

Unlike `ValueClassLike`, `InstanceConverter` is designed for types that:

- Are not wrapper types (they have their own complex structure)
- May fail during conversion (parsing can fail)
- Need custom conversion logic

### Core Operations

```scala
def encode(obj: Obj): Val                            // Convert object to simple representation
def decode(value: Val): Obj                          // Parse simple representation back to object
```

### Error Handling

The `decode` operation can throw a `DecodeErrorException` with detailed error messages:

```scala
class DecodeErrorException(e: Throwable, msg: String) extends IllegalArgumentException(msg, e)
```

### Factory Method

```scala
def apply[Obj: ClassTag, Val](
  _encode: Obj => Val,
  _decode: Val => Obj,
  format: Option[String] = None
): InstanceConverter[Obj, Val]
```

The factory method automatically wraps the decode function with error handling and provides helpful error messages.

### Usage Examples

```scala
import java.util.UUID
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// UUID <-> String conversion
implicit val uuidConverter: InstanceConverter[UUID, String] =
  InstanceConverter(_.toString, UUID.fromString, Some("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"))

// LocalDate <-> String conversion
implicit val dateConverter: InstanceConverter[LocalDate, String] =
  InstanceConverter(
    _.format(DateTimeFormatter.ISO_LOCAL_DATE),
    LocalDate.parse(_, DateTimeFormatter.ISO_LOCAL_DATE),
    Some("yyyy-MM-dd")
  )

// Usage
val uuid = UUID.randomUUID()
val uuidString = uuidConverter.encode(uuid)          // "550e8400-e29b-41d4-a716-446655440000"
val parsedUuid = uuidConverter.decode(uuidString)    // UUID

val date = LocalDate.of(2023, 12, 25)
val dateString = dateConverter.encode(date)          // "2023-12-25"
val parsedDate = dateConverter.decode(dateString)    // LocalDate

// Error handling
try {
  dateConverter.decode("invalid-date")
} catch {
  case e: DecodeErrorException =>
    // "java.time.LocalDate cannot be parsed from invalid-date – should be in format yyyy-MM-dd"
    println(e.getMessage)
}
```

### Pre-built Instances

The `kebs-instances` module provides pre-built `InstanceConverter` instances for common Java types:

```scala
import pl.iterators.kebs.instances.KebsInstances._

// Automatically available converters for:
// - UUID <-> String
// - LocalDate <-> String
// - LocalDateTime <-> String
// - ZonedDateTime <-> String
// - Instant <-> String
// - Duration <-> String
// - URI <-> String
// - URL <-> String
// And many more...
```

## How the Typeclasses Work Together

These typeclasses form a coherent system that enables Kebs to automatically derive instances for various libraries:

### 1. Type Classification

- **ValueClassLike**: For wrapper types (value classes, tagged types, opaque types)
- **EnumLike**: For name-based enumerations
- **ValueEnumLike**: For value-based enumerations
- **InstanceConverter**: For complex types with custom conversion logic

### 2. Automatic Derivation

Each integration module (circe, slick, etc.) provides implicit derivation rules based on these typeclasses:

```scala
// Example from kebs-circe
implicit def valueClassEncoder[VC, F1](implicit
  vcLike: ValueClassLike[VC, F1],
  encoder: Encoder[F1]
): Encoder[VC] = encoder.contramap(vcLike.unapply)

implicit def enumEncoder[T](implicit
  enumLike: EnumLike[T]
): Encoder[T] = Encoder[String].contramap(enumLike.getName)

implicit def instanceEncoder[Obj, Val](implicit
  converter: InstanceConverter[Obj, Val],
  encoder: Encoder[Val]
): Encoder[Obj] = encoder.contramap(converter.encode)
```

### 3. Composability

The typeclasses compose naturally - you can have wrapper types that contain enums or instances:

```scala
case class UserId(value: UUID) extends AnyVal        // ValueClassLike + InstanceConverter
case class UserStatus(status: Status) extends AnyVal // ValueClassLike + EnumLike
```

This design allows Kebs to handle arbitrarily complex domain types while maintaining type safety and performance.
