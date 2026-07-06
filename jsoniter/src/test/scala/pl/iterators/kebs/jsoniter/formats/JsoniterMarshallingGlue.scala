package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.apache.pekko.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.util.ByteString

/** Minimal pekko glue with the same shape as pjfanning's JsoniterScalaSupport: a contravariant marshaller whose type variable is pinned
  * only by the implicit codec parameter.
  */
trait JsoniterMarshallingGlue {
  implicit def marshaller[A](implicit codec: JsonValueCodec[A]): ToEntityMarshaller[A] = {
    val ct = ContentType.WithFixedCharset(MediaTypes.`application/json`)
    Marshaller.withFixedContentType(ct)(a => HttpEntity.Strict(ct, ByteString.fromArrayUnsafe(writeToArray(a))))
  }
}
