package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.net._

trait NetInstances {

  implicit val inetAddressRep: CaseClass1Rep[InetAddress, String] =
    new CaseClass1Rep[InetAddress, String](InetAddress.getByName, _.toString)

  implicit val uriClass1Rep: CaseClass1Rep[URI, String] =
    new CaseClass1Rep[URI, String](new URI(_), _.toString)

  implicit val urlCaseClass1Rep: CaseClass1Rep[URL, String] =
    new CaseClass1Rep[URL, String](new URL(_), _.toString)
}
