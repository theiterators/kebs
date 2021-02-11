package pl.iterators.kebs_examples

import com.github.tminglei.slickpg.ExPostgresProfile
import enumeratum.{Enum, EnumEntry}
import slick.lifted.ProvenShape

object EnumValueColumnType {

  object BeforeKebs {
    import slick.jdbc.PostgresProfile.api._

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
      implicit val workerAccountStatusColumnType: BaseColumnType[WorkerAccountStatus] =
        MappedColumnType.base(_.entryName, WorkerAccountStatus.withName)
    }

    class People(tag: Tag) extends Table[Person](tag, "people") {
      import People._

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
      def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

      protected def mobile = (mobileCountryCode, mobileNumber) <> (Mobile.tupled, Mobile.unapply)
      protected def billingAddress =
        (billingAddressLine1, billingAddressLine2, billingPostalCode, billingCity, billingCountry) <> (Address.tupled, Address.unapply)
      protected def billingInfo =
        (billingAddress, taxId, bankName, bankAccountNumber, recipientName, additionalInfo) <> (BillingInfo.tupled, BillingInfo.unapply)

      override def * : ProvenShape[Person] =
        (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
    }
  }

  object AfterKebs {
    import slick.jdbc.PostgresProfile.api._
    import pl.iterators.kebs._
    import enums._

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
      def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

      protected def mobile = (mobileCountryCode, mobileNumber) <> (Mobile.tupled, Mobile.unapply)
      protected def billingAddress =
        (billingAddressLine1, billingAddressLine2, billingPostalCode, billingCity, billingCountry) <> (Address.tupled, Address.unapply)
      protected def billingInfo =
        (billingAddress, taxId, bankName, bankAccountNumber, recipientName, additionalInfo) <> (BillingInfo.tupled, BillingInfo.unapply)

      override def * : ProvenShape[Person] =
        (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
    }
  }

  object AfterKebsTraitStyle {
    import pl.iterators.kebs.Kebs
    import pl.iterators.kebs.enums.KebsEnums

    object MyPostgresProfile extends ExPostgresProfile {
      override val api: APIWithKebsAndEnums = new APIWithKebsAndEnums {}
      trait APIWithKebsAndEnums extends super.API with Kebs with KebsEnums.Lowercase
    }

    import MyPostgresProfile.api._
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
      def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

      protected def mobile = (mobileCountryCode, mobileNumber) <> (Mobile.tupled, Mobile.unapply)
      protected def billingAddress =
        (billingAddressLine1, billingAddressLine2, billingPostalCode, billingCity, billingCountry) <> (Address.tupled, Address.unapply)
      protected def billingInfo =
        (billingAddress, taxId, bankName, bankAccountNumber, recipientName, additionalInfo) <> (BillingInfo.tupled, BillingInfo.unapply)

      override def * : ProvenShape[Person] =
        (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
    }
  }

  case class UserId(userId: String)             extends AnyVal
  case class EmailAddress(emailAddress: String) extends AnyVal
  case class FullName(fullName: String)         extends AnyVal
  case class PostalCode(postalCode: String)     extends AnyVal
  case class Area(area: String)                 extends AnyVal
  case class City(city: String)                 extends AnyVal
  case class Country(country: String)           extends AnyVal
  case class AddressLine(line: String)          extends AnyVal
  case class TaxId(taxId: String)               extends AnyVal
  case class BankAccountNumber(number: String)  extends AnyVal
  case class BankName(name: String)             extends AnyVal
  case class RecipientName(name: String)        extends AnyVal
  case class AdditionalInfo(content: String)    extends AnyVal

  case class Address(addressLine1: AddressLine, addressLine2: Option[AddressLine], postalCode: PostalCode, city: City, country: Country) {
    override def toString: String = s"${addressLine1.line} ${addressLine2.map(_.line).getOrElse("")}, ${city.city}"
  }

  case class BillingInfo(address: Address,
                         taxId: TaxId,
                         bankName: BankName,
                         bankAccountNumber: BankAccountNumber,
                         recipientName: RecipientName,
                         additionalInfo: AdditionalInfo)

  case class Mobile(countryCode: String, number: String)

  sealed trait WorkerAccountStatus extends EnumEntry
  object WorkerAccountStatus extends Enum[WorkerAccountStatus] {
    case object Unapproved extends WorkerAccountStatus
    case object Active     extends WorkerAccountStatus
    case object Blocked    extends WorkerAccountStatus

    override val values = findValues
  }

  case class Person(userId: UserId,
                    emailAddress: EmailAddress,
                    fullName: FullName,
                    mobile: Mobile,
                    billingInfo: BillingInfo,
                    workCity: City,
                    workArea: Area,
                    status: WorkerAccountStatus)
}
