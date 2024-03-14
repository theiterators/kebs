package pl.iterators.kebs.examples

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
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
