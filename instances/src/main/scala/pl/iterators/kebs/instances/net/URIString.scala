package pl.iterators.kebs.instances.net

import pl.iterators.kebs.instances.InstanceConverter
import pl.iterators.kebs.instances.net.URIString.URIFormat

import java.net.URI

/** Note: [[java.net.InetAddress]] and [[java.net.URL]] are not supported because of DNS lookups performed
  * when constructing [[java.net.InetAddress]] using `getByName(str)` with hostname as the argument,
  * or when comparing [[java.net.URL]] using `equals` method. [[java.net.URI]] should be used instead. */
trait URIString {
  implicit val uriFormatter: InstanceConverter[URI, String] =
    InstanceConverter[URI, String](_.toString, new URI(_), Some(URIFormat))
}
object URIString {
  private[instances] val URIFormat = "RFC 2396 defined format e.g. mailto:your@server.com or https://www.iteratorshq.com/"
}
