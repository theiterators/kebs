package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.net._

trait NetInstances extends Instances {

  implicit def inetAddressRep[T](implicit f: InstancesFormatter[InetAddress, T]): CaseClass1Rep[InetAddress, T] =
    new CaseClass1Rep[InetAddress, T](decodeObject[InetAddress, T](f.decode), f.encode)

  implicit def uriRep[T](implicit f: InstancesFormatter[URI, T]): CaseClass1Rep[URI, T] =
    new CaseClass1Rep[URI, T](decodeObject[URI, T](f.decode), f.encode)

  implicit def urlRep[T](implicit f: InstancesFormatter[URL, T]): CaseClass1Rep[URL, T] =
    new CaseClass1Rep[URL, T](decodeObject[URL, T](f.decode), f.encode)
}

object NetInstances {
  private[instances] val InetAddressFormat = "net format"
  private[instances] val URIFormat         = "URI format"
  private[instances] val URLFormat         = "URL format"

  trait InetAddressString extends NetInstances {
    implicit val inetAddressFormatter: InstancesFormatter[InetAddress, String] = new InstancesFormatter[InetAddress, String] {
      override def encode(obj: InetAddress): String = obj.getHostName
      override def decode(value: String): Either[DecodeError, InetAddress] =
        tryParse[InetAddress, String](InetAddress.getByName, value, classOf[InetAddress], InetAddressFormat)
    }
  }

  trait URIString extends NetInstances {
    implicit val uriFormatter: InstancesFormatter[URI, String] = new InstancesFormatter[URI, String] {
      override def encode(obj: URI): String = obj.toString
      override def decode(value: String): Either[DecodeError, URI] =
        tryParse[URI, String](new URI(_), value, classOf[URI], URIFormat)
    }
  }

  trait URLString extends NetInstances {
    implicit val urlFormatter: InstancesFormatter[URL, String] = new InstancesFormatter[URL, String] {
      override def encode(obj: URL): String = obj.toString
      override def decode(value: String): Either[DecodeError, URL] =
        tryParse[URL, String](new URL(_), value, classOf[URL], URLFormat)
    }
  }

}
