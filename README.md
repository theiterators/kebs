## Kebs
##### Scala library to eliminate boilerplate
[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)]()
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://travis-ci.org/theiterators/kebs.svg?branch=master)](https://travis-ci.org/theiterators/kebs)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com).

### Table of contents
* [Why?](#why)
* [SBT](#sbt)
* [Examples](#examples)
  * [slick](#--kebs-generates-slick-mappers-for-your-case-class-wrappers-kebs-slick)
  * [doobie](#--kebs-generates-doobie-mappers-for-your-case-class-wrappers-kebs-doobie)
  * [spray-json](#--kebs-eliminates-spray-json-induced-boilerplate-kebs-spray-json)
  * [play-json](#--kebs-eliminates-play-json-induced-boilerplate-kebs-play-json)
  * [akka-http](#--kebs-generates-akka-http--pekko-http-unmarshaller-kebs-akka-http--kebs-pekko-http)
  * [http4s](#--kebs-provides-helpers-for-http4s)
  * [circe](#--kebs-eliminates-circe-induced-boilerplate-kebs-circe)
* [Tagged types](#tagged-types)
* [JsonSchema support](#jsonschema-support)
* [Scalacheck support](#scalacheck-support)
* [Kebs for IntelliJ](#kebs-for-intellij)
* [Kebs 2.0 migration guide](#kebs-20-migration-guide)

### Why?

`kebs` is for eliminating some common sources of Scala boilerplate code that arise when you use 
Slick (`kebs-slick`), Doobie (`kebs-doobie`), Spray (`kebs-spray-json`), Play (`kebs-play-json`), Circe (`kebs-circe`), Akka HTTP (`kebs-akka-http`), Pekko HTTP (`kebs-pekko-http`), http4s (`kebs-http4s`).

### SBT

Support for `slick`

`libraryDependencies += "pl.iterators" %% "kebs-slick" % "1.9.6"`

Support for `doobie`

`libraryDependencies += "pl.iterators" %% "kebs-doobie" % "1.9.6"`

Support for `spray-json`

`libraryDependencies += "pl.iterators" %% "kebs-spray-json" % "1.9.6"`

Support for `play-json`

`libraryDependencies += "pl.iterators" %% "kebs-play-json" % "1.9.6"`

Support for `circe`

`libraryDependencies += "pl.iterators" %% "kebs-circe" % "1.9.6"`

Support for `json-schema`

`libraryDependencies += "pl.iterators" %% "kebs-jsonschema" % "1.9.6"`

Support for `scalacheck`

`libraryDependencies += "pl.iterators" %% "kebs-scalacheck" % "1.9.6"`

Support for `akka-http`

`libraryDependencies += "pl.iterators" %% "kebs-akka-http" % "1.9.6"`

Support for `pekko-http`

`libraryDependencies += "pl.iterators" %% "kebs-pekko-http" % "1.9.6"`

Support for `http4s`

`libraryDependencies += "pl.iterators" %% "kebs-http4s" % "1.9.6"`

Support for `tagged types`

`libraryDependencies += "pl.iterators" %% "kebs-tagged" % "1.9.6"`

or for tagged-types code generation support

`libraryDependencies += "pl.iterators" %% "kebs-tagged-meta" % "1.9.6"`
`addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full)`

Support for `pl.iterators.kebs.json.instances`

`libraryDependencies += "pl.iterators" %% "kebs-pl.iterators.kebs.json.instances" % "1.9.6"`

Builds for Scala `2.12` and `2.13` are provided.

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
import pl.iterators.kebs.slick.Kebs
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

**`kebs-slick` supports Postgres HSTORE type**

Instead of writing this:
```scala

object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
  override val api: APIWithHstore = new APIWithHstore {}
  trait APIWithHstore extends super.API with HStoreImplicits {
    val yearMonthIso: Isomorphism[YearMonth, String] = new Isomorphism(_.toString, YearMonth.parse)
  }
}

import MyPostgresProfile.api._
class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTest") {
  def id                                = column[Long]("id")
  def history: Rep[Map[String, String]] = column[Map[String, String]]("history")

  def historyMapped: MappedProjection[Map[YearMonth, Boolean], Map[String, String]] =
    history.<>(h => h.map(kv => yearMonthIso.comap(kv._1) -> kv._2.toBoolean),
               h => Option(h.map(kv => yearMonthIso.map(kv._1) -> kv._2.toString)))

  def * = (id, historyMapped)
}

class HstoreRepository(implicit ec: ExecutionContext) {
  def get(id: Long, yearMonth: YearMonth): DBIO[Option[Boolean]] =
    byIdQuery(id)
            .map(_.history +> yearMonthIso.map(yearMonth).asColumnOf[Option[String]])
            .result
            .map(_.headOption.flatMap(_.map(_.toBoolean)))

  private def byIdQuery(id: Long) = testTable.filter(_.id === id)

  private val testTable = TableQuery[HStoreTestTable]
}

```
you can write this:

```scala

object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
  override val api: APIWithHstore = new APIWithHstore {}
  trait APIWithHstore extends super.API with HStoreImplicits with Kebs with YearMonthString
}

import MyPostgresProfile.api._
class HStoreTestTable(tag: Tag) extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTest") {
  def id                                    = column[Long]("id")
  def history: Rep[Map[YearMonth, Boolean]] = column[Map[YearMonth, Boolean]]("history")

  def * = (id, history)
}

class HstoreRepository(implicit ec: ExecutionContext) {
  def get(id: Long, yearMonth: YearMonth): DBIO[Option[Boolean]] =
    byIdQuery(id)
            .map(_.history +> yearMonth)
            .result
            .map(_.headOption.flatten)

  private def byIdQuery(id: Long) = testTable.filter(_.id === id)
  
  private val testTable = TableQuery[HStoreTestTable]
}

```
Make sure to mix in correct mapping from `pl.iterators.kebs.json.instances`, in this case `YearMonthString`.


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
import pl.iterators.kebs.slick.Kebs
import pl.iterators.kebs.slick.enums.KebsEnums

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

#### - kebs generates doobie mappers for your case-class wrappers (kebs-doobie)

kebs-doobie works similarly to [kebs-slick](#--kebs-generates-slick-mappers-for-your-case-class-wrappers-kebs-slick). It provides doobie's `Meta` pl.iterators.kebs.json.instances for:

* Instances of `ValueClassLike` (value classes, tagged types, opaque types)
* Instances of `InstanceConverter`
* Enumeratum for Scala 2
* Native enums for Scala 3

To make the magic happen, do `import pl.iterators.kebs._` and `import pl.iterators.kebs.slick.enums._` (or `import pl.iterators.kebs.slick.enums.uppercase._` or `import pl.iterators.kebs.slick.enums.lowercase._`).

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
    implicit val urlJsonFormat = new JsonFormat[URI] {
      override def read(json: JsValue): URI = json match {
        case JsString(uri) => Try(new URI(uri)).getOrElse(deserializationError("Invalid URI format"))
        case _             => deserializationError("URI should be string")
      }

      override def write(obj: URI): JsValue = JsString(obj.toString)
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

If you want to further eliminate boilerplate generated by `JsonProtocol` itself, you can import traits
from `kebs-pl.iterators.kebs.json.instances` you need and then `ThingProtocol` looks like this:

```scala
object ThingProtocol extends DefaultJsonProtocol with SprayJsonSupport with KebsSpray with URIString with UUIDString
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


#### - kebs eliminates Circe induced boilerplate (kebs-circe)
**Still in experimental stage!**
Circe might be a source of boilerplate depending on the type of derivation you use - if it's semi-auto derivation, you'll
have to write a lot of encoders/decoders for your case classes:
```scala
object BeforeKebs {
    object ThingProtocol extends CirceProtocol with CirceAkkaHttpSupport {
      import io.circe._
      import io.circe.generic.semiauto._
      implicit val thingCreateRequestEncoder: Encoder[ThingCreateRequest] = deriveEncoder
      implicit val thingCreateRequestDecoder: Decoder[ThingCreateRequest] = deriveDecoder
      implicit val thingIdEncoder: Encoder[ThingId]                       = deriveEncoder
      implicit val thingIdDecoder: Decoder[ThingId]                       = deriveDecoder
      implicit val thingNameEncoder: Encoder[ThingName]                   = deriveEncoder
      implicit val thingNameDecoder: Decoder[ThingName]                   = deriveDecoder
      implicit val thingDescriptionEncoder: Encoder[ThingDescription]     = deriveEncoder
      implicit val thingDescriptionDecoder: Decoder[ThingDescription]     = deriveDecoder
      implicit val tagIdEncoder: Encoder[TagId]                           = deriveEncoder
      implicit val tagIdDecoder: Decoder[TagId]                           = deriveDecoder
      implicit val locationEncoder: Encoder[Location]                     = deriveEncoder
      implicit val locationDecoder: Decoder[Location]                     = deriveDecoder
      implicit val thingEncoder: Encoder[Thing]                           = deriveEncoder
      implicit val thingDecoder: Decoder[Thing]                           = deriveDecoder
      implicit val errorMessageDecoder: Decoder[ErrorMessage]             = deriveDecoder
      implicit val errorMessageEncoder: Encoder[ErrorMessage]             = deriveEncoder
    }
    import ThingProtocol._
    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {

      def createRoute: Route = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> ErrorMessage("Already exists")
          }
        }
      }
    }
  }
```

Kebs can get rid of this for you: 
```scala
object AfterKebs {
    object ThingProtocol extends KebsCirce with CirceProtocol with CirceAkkaHttpSupport
    import ThingProtocol._

    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {

      def createRoute: Route = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> ErrorMessage("Already exists")
          }
        }
      }
    }
  }
```
If you want to disable flat formats, you can mix-in `KebsCirce.NoFlat`:
```scala
object KebsProtocol extends KebsCirce with KebsCirce.NoFlat
```
You can also support snake-case fields in JSON:
```scala
object KebsProtocol extends KebsCirce with KebsCirce.Snakified
```

And capitalized:
```scala
 object KebsProtocol extends KebsCirce with KebsCirce.Capitalized
```

**NOTE for Scala 3 version of kebs-circe**:
1. As of today, there is no support for the @noflat annotation - using it will have no effect.
2. If you're using recursive types - due to [this issue](https://github.com/circe/circe/issues/1980) you'll have to add codecs explicitly in the following way:
```scala
case class R(a: Int, rs: Seq[R]) derives Decoder, Encoder.AsObject
```


3. If you're using flat format or Snakified/Capitalized formats, remember to import `given` pl.iterators.kebs.json.instances, e.g.:
```scala
  object KebsProtocol extends KebsCirce with KebsCirce.Snakified
  import KebsProtocol.{given, _}
  ```
 
 as for NoFlat, it should stay the same:
 ```scala
   object KebsProtocol extends KebsCirce with KebsCirce.NoFlat
  import KebsProtocol._
 ```
#### - kebs generates akka-http / pekko-http Unmarshaller (kebs-akka-http / kebs-pekko-http)

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

#### - kebs provides helpers for http4s

Kebs makes it easy to use 1-element case-classes, opaque types (Scala 3), `enumeratum` or native Scala 3 enums in its DSL:

```scala
import java.util.UUID
import java.time.Year
import java.util.Currency

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.http4s.{given, _}
import pl.iterators.kebs.pl.iterators.kebs.json.instances.KebsInstances._ // optional, if you want pl.iterators.kebs.json.instances support, ex. java.util.Currency

opaque type Age = Int
object Age extends Opaque[Age, Int] {
  override def validate(value: Int): Either[String, Age] =
    if (value < 0) Left("No going back, sorry") else Right(value)
}

case class UserId(id: UUID)

enum Color {
  case Red, Blue, Green
}

object AgeQueryParamDecoderMatcher extends QueryParamDecoderMatcher[Age]("age")
object OptionalYearParamDecoderMatcher extends OptionalQueryParamDecoderMatcher[Year]("year")
object ValidatingColorQueryParamDecoderMatcher extends ValidatingQueryParamDecoderMatcher[Color]("color")

val routes = HttpRoutes.of[IO] {
  case GET -> Root / "WrappedInt" / WrappedInt[Age](age) => ...
  case GET -> Root / "InstanceString" / InstanceString[Currency](currency) => ...
  case GET -> Root / "EnumString" / EnumString[Color](color) => ...
  case GET -> Root / "WrappedUUID" / WrappedUUID[UserId](userId) => ...
  case GET -> Root / "WrappedIntParam" :? AgeQueryParamDecoderMatcher(age) => ...
  case GET -> Root / "InstanceIntParam" :? OptionalYearParamDecoderMatcher(year) => ...
  case GET -> Root / "EnumStringParam" :? ValidatingColorQueryParamDecoderMatcher(color) => ...
}
```

In Scala 2, some more boilerplate is required due to https://github.com/scala/bug/issues/884. See [tests](https://github.com/theiterators/kebs/blob/master/http4s/src/test/scala-2/pl/iterators/kebs/Http4sDslTests.scala)
for more details.

### Tagged types

Starting with version 1.6.0, kebs contain an implementation of, so-called, `tagged types`. If you want to know what a `tagged type` is, please see eg.
[Introduction to Tagged Types](http://www.vlachjosef.com/tagged-types-introduction/) or [Scalaz tagged types description](http://eed3si9n.com/learning-scalaz/Tagged+type.html).
In general, taggging of a type is a mechanism for distinguishing between various pl.iterators.kebs.json.instances of the same type. For instance, you might want to use an `Int` to represent an _user id_ or _purchase id_.
But if you use _just an Int_ the compiler will not protest if you use _purchase id_ integer in place of _user id_ integer and vice versa.
To gain additional type safety you could use 1-element case-class wrappers, or, tagged types. In short, you would create `Int @@ UserId` and `Int @@ PurchaseId` types,
where `@@` is _tag_ operator. Thus, you can distinguish between various usages of `Int` while still retaining all `Int` properties ie. `Int @@ UserId` is still an `Int`, but it is not `Int @@ PurchaseId`.

This representation is very useful at times, but there is some boilerplate involved which kebs strives to eliminate. Let's take a look at examples.
To get only the kebs' implementation of tagged types, please add `kebs-tagged` module to your build. You'll then be able to use tagging:

```scala
import pl.iterators.kebs.tagged._

trait UserId
trait PurchaseId

val userId: Int @@ UserId = 10.taggedWith[UserId] 
val purchaseId: Int @@ PurchaseId = 10.@@[PurchaseId]

val userIds: List[Int @@ UserId] = List(10, 15, 20).@@@[UserId]
val purchaseIds: List[Int @@ PurchaseId] = List(10, 15, 20).taggedWithF[PurchaseId]
```

Additionally, if you want to use tagged types in Slick, just mix-in `pl.iterators.kebs.tagged.slick.SlickSupport` (or `import pl.iterators.kebs.tagged.slick._`).

```scala
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tagged.slick.SlickSupport

object SlickTaggedExample extends SlickSupport {
  trait UserIdTag
  type UserId = Long @@ UserIdTag

  trait EmailTag
  type Email = String @@ EmailTag

  trait FirstNameTag
  type FirstName = String @@ FirstNameTag

  trait LastNameTag
  type LastName = String @@ LastNameTag

  final case class User(id: UserId, email: Email, firstName: Option[FirstName], lastName: Option[LastName], isAdmin: Boolean)

  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id: Rep[UserId]                   = column[UserId]("id")
    def email: Rep[Email]                 = column[Email]("email")
    def firstName: Rep[Option[FirstName]] = column[Option[FirstName]]("first_name")
    def lastName: Rep[Option[LastName]]   = column[Option[LastName]]("last_name")
    def isAdmin: Rep[Boolean]             = column[Boolean]("is_admin")

    override def * : ProvenShape[User] =
      (id, email, firstName, lastName, isAdmin) <> (User.tupled, User.unapply)
  }

}
```

More often than not, you want to perform some validation before tagging, or, you just want to have a smart constructor that will return tagged representation
whenever criteria are met. You do not have to write it by hand, you can just use `kebs-tagged-meta` which generates all this code for you using `scalameta`.
You just have to tag an object, or a trait, containing your tagged types with `@tagged` annotation.

```scala
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta.tagged

@tagged object Tags {
  trait NameTag
  trait IdTag[+A]
  trait PositiveIntTag

  type Name  = String @@ NameTag
  type Id[A] = Int @@ IdTag[A]

  type PositiveInt = Int @@ PositiveIntTag
  object PositiveInt {
    sealed trait Error
    case object Negative extends Error
    case object Zero     extends Error

    def validate(i: Int) = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
  }
}
```

The annotation will translate your code to something like

```scala
object Tags {
  trait NameTag
  trait IdTag[+A]
  trait PositiveIntTag
  
  type Name = String @@ NameTag
  type Id[A] = Int @@ IdTag[A]
  type PositiveInt = Int @@ PositiveIntTag
  
  object Name {
    def apply(arg: String) = from(arg)
    def from(arg: String) = arg.taggedWith[NameTag]
  }
  object Id {
    def apply[A](arg: Int) = from[A](arg)
    def from[A](arg: Int) = arg.taggedWith[IdTag[A]]
  }
  
  object PositiveInt {
    sealed trait Error
    case object Negative extends Error
    case object Zero extends Error
    def validate(i: Int) = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
    
    def apply(arg: Int) = from(arg).getOrElse(throw new IllegalArgumentException(arg.toString))
    def from(arg: Int) = validate(arg).right.map(arg1 => arg1.taggedWith[PositiveIntTag])
  }
  
  object PositiveIntTag {
    implicit val PositiveIntValueClassLike = new ValueClassLike[PositiveInt, Int](PositiveInt.apply(_), identity)
  }
  object IdTag {
    implicit def IdValueClassLike[A] = new ValueClassLike[Id[A], Int](Id.apply(_), identity)
  }
  object NameTag {
    implicit val NameValueClassLike = new ValueClassLike[Name, String](Name.apply(_), identity)
  }
}
```

You can use generated `from` and `apply` methods as constructors of tagged type instance.

```scala
trait User

val someone = Name("Someone")
//someone: String @@ Tags.NameTag = Someone

val userId = Id[User](10)
//userId: Int @@ Tags.IdTag[User] = 10

val right = PositiveInt.from(10)
//right: scala.util.Either[Tags.PositiveInt.Error,Int @@ Tags.PositiveIntTag] = Right(10)

val notRight = PositiveInt.from(-10)
//notRight: scala.util.Either[Tags.PositiveInt.Error,Int @@ Tags.PositiveIntTag] = Left(Negative)

val alsoRight = PositiveInt(10)
//alsoRight: Int @@ Tags.PositiveIntTag = 10

PositiveInt(-10)
// java.lang.IllegalArgumentException: -10
```

There are some conventions that are assumed during generation. 
* tags have to be empty traits (possibly generic)
* tagged types have to be aliases in form of `type X = SomeType @@ Tag` (possibly generic)
* validation methods for tagged type X have to be defined in `object X` and have to:
  * be public
  * be named `validate`
  * take no type parameters
  * take a single argument
  * return Either (this is not enforced though - you'll have a compilation error later)

Also, `ValueClassLike` is generated for each tag meaning you will get a lot of `kebs` machinery for free eg. spray formats etc.

### Opaque types

As an alternative to tagged types, Scala 3 provides [opaque types](https://docs.scala-lang.org/scala3/reference/other-new-features/opaques.html).
The principles of opaque types are similar to tagged type. The basic usage of opaque types requires the
same amount of boilerplate as tagged types - e.g. you have to write smart constructors, validations and unwrapping
mechanisms all by hand. `kebs-opaque` is meant to help with that by generating a handful of methods and providing a
`ValueClassLike` for an easy typclass derivation.

```scala
import pl.iterators.kebs.opaque._

object MyDomain {
  opaque type ISBN = String
  object ISBN extends Opaque[ISBN, String]
}
```

That's the basic usage. Inside the companion object you will get methods like `from`, `apply`, `unsafe` and extension
method `unwrap` plus an instance of `ValueClassLike[ISBN, String]`. A more complete example below.

```scala
import pl.iterators.kebs.macros.ValueClassLike
import pl.iterators.kebs.opaque._

object MyDomain {
  opaque type ISBN = String
  object ISBN extends Opaque[ISBN, String] {
    override protected def validate(unwrapped: String): Either[String, ISBN] = {
      val trimmed = unwrapped.trim
      val allDigits = trimmed.forall(_.isDigit)
      if (allDigits && trimmed.length == 9) Right("0" + trimmed) // converting old style ISBN to a new one
      else if (allDigits && trimmed.length == 10) Right(trimmed)
      else Left(s"Invalid ISBN: $trimmed")
    }
  }
}

import MyDomain._
ISBN.from("1234567890") // Right(ISBN("1234567890"))
ISBN.from(" 123456789  ") // Right(ISBN("023456789"))
ISBN.from("foo") // Left("Invalid ISBN: foo")

val isbn = ISBN("1234567890") // ISBN("1234567890")
isbn.unwrap // "1234567890"
ISBN("foo") // throws IllegalArgumentException("Invalid ISBN: foo")

ISBN.unsafe("boom") // don't do that, unless you really need to!

trait Showable[A] {
  def show(a: A): String
}
given Showable[String] = (a: String) => a
given[S, A](using showable: Showable[S], vcLike: ValueClassLike[A, S]): Showable[A] = (a: A) => showable.show(vcLike.unapply(a))
implicitly[Showable[ISBN]].show(ISBN("1234567890")) // "1234567890"
```

### JsonSchema support

**Still at experimental stage.**

Kebs contains a macro which generates wrapped Json Schema object of `https://github.com/andyglow/scala-jsonschema`.
Kebs also provides proper implicits conversions for their tagged types and common Java types.
To get your json schema you need to use import pl.iterators.kebs.jsonschema.KebsJsonSchema
(together with pl.iterators.kebs.jsonschema.KebsJsonSchemaPredefs if you need support for more Java types).

```scala
import com.github.andyglow.json.JsonFormatter
import com.github.andyglow.jsonschema.AsValue
import json.schema.Version.Draft07
import pl.iterators.kebs.jsonschema.{KebsJsonSchema, JsonSchemaWrapper}

case class WrappedInt(int: Int)
case class WrappedIntAnyVal(int: Int) extends AnyVal
case class Sample(someNumber: Int,
                  someText: String,
                  arrayOfNumbers: List[Int],
                  wrappedNumber: WrappedInt,
                  wrappedNumberAnyVal: WrappedIntAnyVal)

object Sample extends KebsJsonSchema {

  object SchemaPrinter {
    def printWrapper[T](id: String = "id")(implicit schemaWrapper: JsonSchemaWrapper[T]): String =
      JsonFormatter.format(AsValue.schema(schemaWrapper.schema, Draft07(id)))
  }

  SchemaPrinter.printWrapper[Sample]()

}

```

### Scalacheck support

**Still at experimental stage.**

Kebs provides support to use tagged types in your Arbitrary pl.iterators.kebs.json.instances from ScalaCheck.
Additionally, Kebs provides support for Java types.
Kebs also introduces term of minimal and maximal generator.
The minimal generator is a generator which always generates empty collection of Option, Set, Map etc.
The maximum - in the opposite - always generates non-empty collections.
Kebs provides an useful trait called AllGenerators which binds minimal, normal and maximal generator all together,
so you can easily generate the representation you currently need for tests.

```scala
import pl.iterators.kebs.scalacheck.{KebsArbitraryPredefs, KebsScalacheckGenerators}
import java.net.{URI, URL}
import java.time.{Duration, Instant, LocalDate, LocalDateTime, LocalTime, ZonedDateTime}

case class WrappedInt(int: Int)
case class WrappedIntAnyVal(int: Int) extends AnyVal
case class BasicSample(
    someNumber: Int,
    someText: String,
    wrappedNumber: WrappedInt,
    wrappedNumberAnyVal: WrappedIntAnyVal,
)

case class CollectionsSample(
    listOfNumbers: List[Int],
    arrayOfNumbers: Array[Int],
    setOfNumbers: Set[Int],
    vectorOfNumbers: Vector[Int],
    optionOfNumber: Option[Int],
    mapOfNumberString: Map[Int, String],
)

case class JavaTypesSample(
    instant: Instant,
    zonedDateTime: ZonedDateTime,
    localDateTime: LocalDateTime,
    localDate: LocalDate,
    localTime: LocalTime,
    duration: Duration,
    url: URL,
    uri: URI
)

object Sample extends KebsScalacheckGenerators with KebsArbitraryPredefs {

    val basic = allGenerators[BasicSample].normal.generate

    val minimalCollections = allGenerators[CollectionsSample].minimal.generate
    val maximalCollections = allGenerators[CollectionsSample].maximal.generate

    val javaTypes =  allGenerators[JavaTypesSample].normal.generate

}

```

### Kebs for IntelliJ

The code generated by macros in `kebs-tagged-meta` is not visible to IntelliJ IDEA. There is [Kebs for IntelliJ](https://github.com/theiterators/kebs-intellij)
plugin that enhances experience with the library by adding support for generated code. You can install it from the IntelliJ Marketplace.
In the Settings/Preferences dialog, select "Plugins" and type "Kebs" into search input (see https://www.jetbrains.com/help/idea/managing-plugins.html for detailed instructions).
You can also use this web page: https://plugins.jetbrains.com/plugin/16069-kebs.

### Kebs 2.0 migration guide

Please be aware that recent changes in the source code might require some changes in your codebase. Follow the guide below to migrate your code to Kebs 2.0:
* If you are using value classes instead of tagged/opaque types, please mix in the `CaseClass1ToValueClass` trait.
* Extend your value-enums with `pl.iterators.kebs.slick.enums.ValueEnumLikeEntry` parameterized with the type of the value.
  * Native Scala 3 value-enums:
    ```scala
    enum ColorButRGB(val value: Int) extends ValueEnumLikeEntry[Int] {
      case Red extends ColorButRGB(0xFF0000)
      case Green extends ColorButRGB(0x00FF00)
      case Blue extends ColorButRGB(0x0000FF)
    }
    ```
  * enumeratum value-enums for Scala 2 and Scala 3:
    ```scala
    sealed abstract class LibraryItem(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
      object LibraryItem extends IntEnum[LibraryItem] {
      case object Book extends LibraryItem(value = 1)
      case object Movie extends LibraryItem(value = 2)
      case object Magazine extends LibraryItem(3)
      case object CD extends LibraryItem(4)
      val values = findValues
    }
    ```
* Extend your traits/classes/objects, if inside of one an implicit enum (or value-enum) conversion for `kebs` library's needs should occur, with one of the following traits:
    * For Scala 2 and Scala 3 enums from `enumeratum` library: `pl.iterators.kebs.enumeratum.KebsEnumeratum`
    * For Scala 2 and Scala 3 value-enums from `enumeratum` library: `pl.iterators.kebs.enumeratum.KebsValueEnumeratum`
    * For Scala 3 native value-enums: `pl.iterators.kebs.slick.enums.KebsValueEnum`
    * For Scala 2 `scala.Enumeration` enums or Scala 3 native enums: `pl.iterators.kebs.enums.KebsEnum`
 
