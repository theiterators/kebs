## Kebs

[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.12.svg)]()
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)


![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

### Table of contents
* [Why?](#why)
* [SBT](#sbt)
* [Examples](#examples)
  * [slick](#--kebs-generates-slick-mappers-for-your-case-class-wrappers-kebs-slick)
  * [spray-json](#--kebs-eliminates-spray-json-induced-boilerplate-kebs-spray-json)
  * [play-json](#--kebs-eliminates-play-json-induced-boilerplate-kebs-play-json)
  * [akka-http](#--kebs-generates-akka-http-unmarshaller-kebs-akka-http)
  * [avro4s](#--kebs-generates-avro-schemasserializersdeserializers-for-value-types-kebs-avro)

### Why?

`kebs` is for eliminating some common sources of boilerplate code that arise when you use 
Slick (`kebs-slick`), Spray (`kebs-spray-json`), Play (`kebs-play-json`), Akka HTTP (`kebs-akka-http`) or Avro4s (`kebs-avro`).

### SBT

Support for `slick`

`libraryDependencies += "pl.iterators" %% "kebs-slick" % "1.5.3"`

Support for `spray-json`

`libraryDependencies += "pl.iterators" %% "kebs-spray-json" % "1.5.3"`

Support for `play-json`

`libraryDependencies += "pl.iterators" %% "kebs-play-json" % "1.5.3"`

Support for `akka-http`

`libraryDependencies += "pl.iterators" %% "kebs-akka-http" % "1.5.3"`

Support for `avro4s`

`libraryDependencies += "pl.iterators" %% "kebs-avro" % "1.5.3"`

Builds for Scala `2.11` and `2.12` are provided

### Examples

Please check out [examples](https://github.com/theiterators/kebs/tree/master/examples/src/main/scala/pl/iterators/kebs_examples)

#### - kebs generates slick mappers for your case-class wrappers (kebs-slick)

If you want to model the following table

```scala

case class UserId(userId: String)             extends AnyVal
case class EmailAddress(emailAddress: String) extends AnyVal
case class FullName(fullName: String)         extends AnyVal

//...

class People(tag: Tag) extends Table[Person](tag, "people") {
  def userId: Rep[UserId]                           = column[UserId]("user_id")
  def emailAddress: Rep[EmailAddress]               = column[EmailAddress]("email_address")
  def fullName: Rep[FullName]                       = column[FullName]("full_name")
  def mobileCountryCode: Rep[String]                = column[String]("mobile_country_code")
  def mobileNumber: Rep[String]                     = column[String]("mobile_number")
  def billingAddressLine1: Rep[AddressLine]         = column[AddressLine]("billing_address_line1")
  def billingAddressLine2: Rep[Option[AddressLine]] = column[Option[AddressLine]]("billing_address_line2")
  def billingPostalCode: Rep[PostalCode]            = column[PostalCode]("billing_postal_code")
  def billingCity: Rep[City]                        = column[City]("billing_city")
  def billingCountry: Rep[Country]                  = column[Country]("billing_country")
  def taxId: Rep[TaxId]                             = column[TaxId]("tax_id")
  def bankName: Rep[BankName]                       = column[BankName]("bank_name")
  def bankAccountNumber: Rep[BankAccountNumber]     = column[BankAccountNumber]("bank_account_number")
  def recipientName: Rep[RecipientName]             = column[RecipientName]("recipient_name")
  def additionalInfo: Rep[AdditionalInfo]           = column[AdditionalInfo]("additional_info")
  def workCity: Rep[City]                           = column[City]("work_city")
  def workArea: Rep[Area]                           = column[Area]("work_area")

  protected def mobile = (mobileCountryCode, mobileNumber) <> (Mobile.tupled, Mobile.unapply)
  protected def billingAddress =
    (billingAddressLine1, billingAddressLine2, billingPostalCode, billingCity, billingCountry) <> (Address.tupled, Address.unapply)
  protected def billingInfo =
    (billingAddress, taxId, bankName, bankAccountNumber, recipientName, additionalInfo) <> (BillingInfo.tupled, BillingInfo.unapply)

  override def * : ProvenShape[Person] =
    (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea) <> (Person.tupled, Person.unapply)
}

```

then you are forced to write this:

```scala
object People {
  implicit val userIdColumnType: BaseColumnType[UserId]                 = MappedColumnType.base(_.userId, UserId.apply)
  implicit val emailAddressColumnType: BaseColumnType[EmailAddress]     = MappedColumnType.base(_.emailAddress, EmailAddress.apply)
  implicit val fullNameColumnType: BaseColumnType[FullName]             = MappedColumnType.base(_.fullName, FullName.apply)
  implicit val addressLineColumnType: BaseColumnType[AddressLine]       = MappedColumnType.base(_.line, AddressLine.apply)
  implicit val postalCodeColumnType: BaseColumnType[PostalCode]         = MappedColumnType.base(_.postalCode, PostalCode.apply)
  implicit val cityColumnType: BaseColumnType[City]                     = MappedColumnType.base(_.city, City.apply)
  implicit val areaColumnType: BaseColumnType[Area]                     = MappedColumnType.base(_.area, Area.apply)
  implicit val countryColumnType: BaseColumnType[Country]               = MappedColumnType.base(_.country, Country.apply)
  implicit val taxIdColumnType: BaseColumnType[TaxId]                   = MappedColumnType.base(_.taxId, TaxId.apply)
  implicit val bankNameColumnType: BaseColumnType[BankName]             = MappedColumnType.base(_.name, BankName.apply)
  implicit val recipientNameColumnType: BaseColumnType[RecipientName]   = MappedColumnType.base(_.name, RecipientName.apply)
  implicit val additionalInfoColumnType: BaseColumnType[AdditionalInfo] = MappedColumnType.base(_.content, AdditionalInfo.apply)
  implicit val bankAccountNumberColumnType: BaseColumnType[BankAccountNumber] =
    MappedColumnType.base(_.number, BankAccountNumber.apply)
}
    
```

**`kebs` can do it automagically for you**

```scala

import pl.iterators.kebs._

class People(tag: Tag) extends Table[Person](tag, "people") {
  def userId: Rep[UserId]                           = column[UserId]("user_id")
  //...
}

```

If you prefer to **mix in trait** instead of import (for example you're using a custom driver like `slick-pg`), you can do it as well:

```scala
import pl.iterators.kebs.Kebs
object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with Kebs
}

import MyPostgresProfile.api._
```

**`kebs-slick` can also generate mappings for Postgres ARRAY type**, which is a common source of boilerplate in `slick-pg`.
Instead of:

```scala

object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits {
    implicit val institutionListTypeWrapper =
      new SimpleArrayJdbcType[Long]("int8").mapTo[Institution](Institution, _.value).to(_.toList)
    implicit val marketFinancialProductWrapper =
      new SimpleArrayJdbcType[String]("text").mapTo[MarketFinancialProduct](MarketFinancialProduct, _.value).to(_.toList)
  }
}

import MyPostgresProfile.api._

class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
  def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def institutions         = column[List[Institution]]("institutions")
  def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

  def * = (id, institutions, mktFinancialProducts)
}

```

you can do just:

```scala

object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with Kebs
}

import MyPostgresProfile.api._
class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
  def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def institutions         = column[List[Institution]]("institutions")
  def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

  def * = (id, institutions, mktFinancialProducts)
}

```

**`kebs` also supports `Enumeratum`**
Let's go back to the previous example. If you wanted to add a column of type `EnumEntry`, then you would have to write mapping for it:

```scala

sealed trait WorkerAccountStatus extends EnumEntry
object WorkerAccountStatus extends Enum[WorkerAccountStatus] {
  case object Unapproved extends WorkerAccountStatus
  case object Active     extends WorkerAccountStatus
  case object Blocked    extends WorkerAccountStatus

  override val values = findValues
}

object People {

  //...
  
  implicit val workerAccountStatusColumnType: BaseColumnType[WorkerAccountStatus] =
    MappedColumnType.base(_.entryName, WorkerAccountStatus.withName)
}

class People(tag: Tag) extends Table[Person](tag, "people") {
  import People._

  //...
  
  def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

  //...

  override def * : ProvenShape[Person] =
    (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
}

```

`kebs` takes care of this as well:

```scala

import pl.iterators.kebs._
import enums._

class People(tag: Tag) extends Table[Person](tag, "people") {

  //...

  def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

  //...

  override def * : ProvenShape[Person] =
    (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
  }

```

You can also choose between a few **strategies of writing enums**.
If you just import `enums._`, then you'll get its `entryName` in db. 
If you import `enums.lowercase._` or `enums.uppercase._` then it'll save enum name in, respectively, lower or upper case
Of course, enums also work with traits:

```scala
import pl.iterators.kebs.Kebs
import pl.iterators.kebs.enums.KebsEnums

object MyPostgresProfile extends ExPostgresDriver {
  override val api: API = new API {}
  trait API extends super.API with Kebs with KebsEnums.Lowercase /* or KebsEnums, KebsEnums.Uppercase etc. */
}

import MyPostgresProfile.api._
```

`kebs` also supports `ValueEnum`s, to save something other than entry's name to db. For example, if you wanted `WorkerAccountStatus` to be saved as int value, you'd write:
 
 ```scala
 sealed abstract class WorkerAccountStatusInt(val value: Int) extends IntEnumEntry
 object WorkerAccountStatusInt extends IntEnum[WorkerAccountStatusInt] {
     case object Unapproved extends WorkerAccountStatusInt(0)
     case object Active     extends WorkerAccountStatusInt(1)
     case object Blocked    extends WorkerAccountStatusInt(2)
    
     override val values = findValues
 }
 ```

#### - kebs eliminates spray-json induced boilerplate (kebs-spray-json)

Writing JSON formats in spray can be really unwieldy. For every case-class you want serialized, you have to count the number of fields it has.
And if you want a 'flat' format for 1-element case classes, you have to wire it yourself


```scala
def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
new JsonFormat[T] {
  override def read(json: JsValue): T = construct(jr.read(json))
  override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
}
```

All of this can be left to `kebs-spray-json`. Let's pretend we are to write an `akka-http` router:

```scala
class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {
  import ThingProtocol._
  def createRoute = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
    complete {
      thingsService.create(request).map[ToResponseMarshallable] {
        case ThingCreateResponse.Created(thing) => Created  -> thing
        case ThingCreateResponse.AlreadyExists  => Conflict -> Error("Already exists")
      }
    }
  }
}
```

The source of boilerplate is `ThingProtocol` which can grow really big

```scala
 trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val urlJsonFormat = new JsonFormat[URL] {
      override def read(json: JsValue): URL = json match {
        case JsString(url) => Try(new URL(url)).getOrElse(deserializationError("Invalid URL format"))
        case _             => deserializationError("URL should be string")
      }

      override def write(obj: URL): JsValue = JsString(obj.toString)
    }

    implicit val uuidFormat = new JsonFormat[UUID] {
      override def write(obj: UUID): JsValue = JsString(obj.toString)

      override def read(json: JsValue): UUID = json match {
        case JsString(uuid) => Try(UUID.fromString(uuid)).getOrElse(deserializationError("Expected UUID format"))
        case _              => deserializationError("Expected UUID format")
      }
    }
  }

object ThingProtocol extends JsonProtocol {
  def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
    new JsonFormat[T] {
      override def read(json: JsValue): T = construct(jr.read(json))
      override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
    }

  implicit val errorJsonFormat              = jsonFormat1(Error.apply)
  implicit val thingIdJsonFormat            = jsonFlatFormat(ThingId.apply)
  implicit val tagIdJsonFormat              = jsonFlatFormat(TagId.apply)
  implicit val thingNameJsonFormat          = jsonFlatFormat(ThingName.apply)
  implicit val thingDescriptionJsonFormat   = jsonFlatFormat(ThingDescription.apply)
  implicit val locationJsonFormat           = jsonFormat2(Location.apply)
  implicit val createThingRequestJsonFormat = jsonFormat5(ThingCreateRequest.apply)
  implicit val thingJsonFormat              = jsonFormat6(Thing.apply)
}
```

But all of this can be generated automatically, can't it? You only need to import `KebsSpray` trait and you're done:

```scala
object ThingProtocol extends JsonProtocol with KebsSpray
```

Additionally, `kebs-spray-json` tries hard to be smart. It prefers 'flat' format when it comes across 1-element case-classes
In case like this:

```scala
case class ThingId(uuid: UUID)
case class ThingName(name: String)

case class Thing(id: ThingId, name: ThingName, ...)
```

it'll do what you probably expected - `{"id": "uuid", "name": "str"}`. But it also takes into account
if you want `RootJsonFormat` or not. So `case class Error(message: String)` in `Conflict -> Error("Already exists")` will be formatted as
`{"message": "Already exists"}` in JSON. 

What if you do not want to use 'flat' format by default?
You have three options to choose from:
* redefine implicits for case-classes you want serialized 'non-flat'
```scala
case class Book(name: String, chapters: List[Chapter])
case class Chapter(name: String)

implicit val chapterRootFormat: RootJsonFormat[Chapter] = jsonFormatN[Chapter]

test("work with nested single field objects") {
    val json =
      """
        | {
        |   "name": "Functional Programming in Scala",
        |   "chapters": [{"name":"first"}, {"name":"second"}]
        | }
      """.stripMargin
    
    json.parseJson.convertTo[Book] shouldBe Book(
      name = "Functional Programming in Scala",
      chapters = List(Chapter("first"), Chapter("second"))
    )
}
```

* mix-in `KebsSpray.NonFlat` if you want _flat_ format to become globally turned off for a protocol
```scala
object KebsProtocol extends DefaultJsonProtocol with KebsSpray.NoFlat
```

* use `noflat` annotation on selected case-classes (thanks to @dbronecki)
```scala
case class Book(name: String, chapters: List[Chapter])
@noflat case class Chapter(name: String)
```


Often you have to deal with convention to have **`snake-case` fields in JSON**.
That's something `kebs-spray-json` can do for you as well

```scala
object ThingProtocol extends JsonProtocol with KebsSpray.Snakified
```

Another advantage is that _snakified_ names are computed during computation, so in run-time they're just string constants.

`kebs-spray-json` also can deal with `enumeratum` enums.

```scala
object ThingProtocol extends JsonProtocol with KebsSpray with KebsEnumFormats
```

As in slick's example, you have two additional enum serialization strategies: 
_uppercase_ i _lowercase_ (`KebsEnumFormats.Uppercase`, `KebsEnumFormats.Lowercase`), as well as support for `ValueEnumEntry`

It can also generate recursive formats via `jsonFormatRec` macro, as in the following example:

```scala
case class Thing(thingId: String, parent: Option[Thing])
implicit val thingFormat: RootJsonFormat[Thing] = jsonFormatRec[Thing]
```

`kebs-spray-json` also provides JSON formats for case classes with more than 22 fields.

#### - kebs eliminates play-json induced boilerplate (kebs-play-json)

To be honest `play-json` has never been a source of extensive boilerplate for me- thanks to `Json.format[CC]` macro.
Only _flat_ formats have had to be written over and over. ~~And there is no support for `enumeratum`~~ (support for `enumeratum` is provided by `enumeratum-play-json` and has been removed from `kebs`).
So if you find yourself writing lots of code similar to:

```scala
def flatFormat[P, T <: Product](construct: P => T)(implicit jf: Format[P]): Format[T] =
  Format[T](jf.map(construct), Writes(a => jf.writes(a.productElement(0).asInstanceOf[P])))

implicit val thingIdJsonFormat          = flatFormat(ThingId.apply)
implicit val tagIdJsonFormat            = flatFormat(TagId.apply)
implicit val thingNameJsonFormat        = flatFormat(ThingName.apply)
implicit val thingDescriptionJsonFormat = flatFormat(ThingDescription.apply)

implicit val errorJsonFormat              = Json.format[Error]
implicit val locationJsonFormat           = Json.format[Location]
implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
implicit val thingJsonFormat              = Json.format[Thing]
```

, you can delegate it to `kebs-play-json`

```scala
import pl.iterators.kebs.json._
  
implicit val errorJsonFormat              = Json.format[Error]
implicit val locationJsonFormat           = Json.format[Location]
implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
implicit val thingJsonFormat              = Json.format[Thing]
```

(or, trait-style)

```scala
object AfterKebs extends JsonProtocol with KebsPlay {
implicit val errorJsonFormat              = Json.format[Error]
implicit val locationJsonFormat           = Json.format[Location]
implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
implicit val thingJsonFormat              = Json.format[Thing]
}
```

#### - kebs generates akka-http Unmarshaller (kebs-akka-http)

It makes it very easy to use 1-element case-classes or `enumeratum` enums/value enums in eg. `parameters` directive:

```scala
sealed abstract class Column(val value: Int) extends IntEnumEntry
object Column extends IntEnum[Column] {
    case object Name extends Column(1)
    case object Date extends Column(2)
    case object Type extends Column(3)
    
    override val values = findValues
}

sealed trait SortOrder extends EnumEntry
object SortOrder extends Enum[SortOrder] {
    case object Asc  extends SortOrder
    case object Desc extends SortOrder
    
    override val values = findValues
}

case class Offset(value: Int) extends AnyVal
case class Limit(value: Int)  extends AnyVal

case class PaginationQuery(sortBy: Column, sortOrder: SortOrder, offset: Offset, limit: Limit)

import pl.iterators.kebs.unmarshallers._
import enums._

val route = get {
  parameters('sortBy.as[Column], 'order.as[SortOrder] ? (SortOrder.Desc: SortOrder), 'offset.as[Offset] ? Offset(0), 'limit.as[Limit])
    .as(PaginationQuery) { query =>
      //...
    }

}

```

#### - kebs generates Avro schemas/serializers/deserializers for value types (kebs-avro)


If you use _value types_ and `avro4s`, you might be disappointed to hear that `avro4s` generates avro _records_ even if your case-class extends from `AnyVal`. For example:

```scala
case class Ingredient(name: String) extends AnyVal
case class Pizza(name: String, 
                 ingredients: Seq[Ingredient], 
                 vegetarian: Boolean, 
                 vegan: Boolean, 
                 calories: Int)

AvroSchema[Pizza]

```

will generate

```json
{
  "type":"record",
  "name":"Pizza",
  "namespace":"<empty>",
  "fields":[{"name":"name",
             "type":"string"},
            {"name":"ingredients",
             "type":{"type":"array",
                     "items":{
                              "type":"record",
                              "name":"Ingredient",
                              "fields":[{"name":"name","type":"string"}]
                             }
                    }
            }, //...
}

```

As you can see even though `Ingredient` is just a wrapper for a string value, it doesn't get reflected in the schema.
With `kebs-avro` though, such case-classes will be represented as primitive types (just `import pl.iterators.kebs.avro._` )

```json
{
  "type":"record",
  "name":"Pizza",
  "namespace":"<empty>",
  "fields":[{"name":"name",
             "type":"string"},
            {"name":"ingredients",
             "type":{"type":"array","items":"string"}}, //...
}
```
