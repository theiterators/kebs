package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.net.URI

/** Note: [[java.net.InetAddress]] and [[java.net.URL]] are not supported because of DNS lookups performed
  * when constructing [[java.net.InetAddress]] using `getByName(str)` with hostname as the argument,
  * or when comparing [[java.net.URL]] using `equals` method. [[java.net.URI]] should be used instead. */
trait NetInstances extends Instances {

  implicit def uriRep[T](implicit f: InstancesFormatter[URI, T]): CaseClass1Rep[URI, T] =
    new CaseClass1Rep[URI, T](decodeObject[URI, T](f.decode), f.encode)

}

object NetInstances {
  private[instances] val URIFormat = "RFC 2396 defined format e.g. mailto:your@server.com or https://www.iteratorshq.com/"

  trait URIString extends NetInstances {
    implicit val uriFormatter: InstancesFormatter[URI, String] = new InstancesFormatter[URI, String] {
      override def encode(obj: URI): String = obj.toString
      override def decode(value: String): Either[DecodeError, URI] =
        tryParse[URI, String](new URI(_), value, classOf[URI], URIFormat)
    }
  }

}
