---
sidebar_position: 3
title: Slick
---

# kebs-slick

Automatic column type mappings for Slick.

## Setup

```scala
libraryDependencies += "pl.iterators" %% "kebs-slick" % kebsVersion
```

## Value class mappings

Instead of writing manual `MappedColumnType.base` for every wrapper type:

```scala
case class UserId(userId: String) extends AnyVal
case class EmailAddress(emailAddress: String) extends AnyVal

// without kebs — manual mappings for each type
object People {
  implicit val userIdColumnType: BaseColumnType[UserId] =
    MappedColumnType.base(_.userId, UserId.apply)
  implicit val emailAddressColumnType: BaseColumnType[EmailAddress] =
    MappedColumnType.base(_.emailAddress, EmailAddress.apply)
  // ... and so on for every type
}
```

Mix in `KebsSlickSupport` and `CaseClass1ToValueClass`:

```scala
import pl.iterators.kebs.slick.KebsSlickSupport
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass

class People(tag: Tag) extends Table[Person](tag, "people")
    with KebsSlickSupport with CaseClass1ToValueClass {
  def userId: Rep[UserId] = column[UserId]("user_id")
  // ...
}
```

With a custom driver (e.g. slick-pg), mix in to the profile API:

```scala
import pl.iterators.kebs.slick.KebsSlickSupport

object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with KebsSlickSupport
}
```

## Postgres array support

Works automatically when mixing `KebsSlickSupport` with `ArrayImplicits`:

```scala
object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with KebsSlickSupport
}

import MyPostgresProfile.api._

class ArrayTestTable(tag: Tag)
    extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
  def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def institutions         = column[List[Institution]]("institutions")
  def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")
  def *                    = (id, institutions, mktFinancialProducts)
}
```

## Postgres hstore support

Mix in `KebsSlickSupport` with `HStoreImplicits` and the appropriate instance (e.g. `YearMonthString`):

```scala
import pl.iterators.kebs.instances.time.YearMonthString

object MyPostgresProfile extends ExPostgresProfile with PgHStoreSupport {
  override val api: APIWithHstore = new APIWithHstore {}
  trait APIWithHstore extends super.API with HStoreImplicits with KebsSlickSupport with YearMonthString
}

import MyPostgresProfile.api._

class HStoreTestTable(tag: Tag)
    extends Table[(Long, Map[YearMonth, Boolean])](tag, "HStoreTest") {
  def id      = column[Long]("id")
  def history = column[Map[YearMonth, Boolean]]("history")
  def *       = (id, history)
}
```

## Enum support

For Enumeratum (Scala 2 & 3):

```scala
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}

object MyPostgresProfile extends ExPostgresDriver {
  override val api: API = new API {}
  trait API extends super.API with KebsSlickSupport with KebsEnumeratum with KebsValueEnumeratum
}
```

For Scala 3 native enums:

```scala
import pl.iterators.kebs.enums.{KebsEnum, KebsValueEnum}

object MyPostgresProfile extends ExPostgresDriver {
  override val api: API = new API {}
  trait API extends super.API with KebsSlickSupport with KebsEnum with KebsValueEnum
}
```

Value enums (storing numeric/other values instead of names) require extending your entries with `ValueEnumLikeEntry`.
