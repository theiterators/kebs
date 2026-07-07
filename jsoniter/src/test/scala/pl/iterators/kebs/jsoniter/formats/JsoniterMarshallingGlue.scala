package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import org.apache.pekko.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.util.ByteString

/*
 * Basically for any type A that has JsonValueCodec[A] in scope it generates ToEntityMarshaller[A] -- useful for testing pekko synchronization
 */
trait JsoniterMarshallingGlue {
  implicit def marshaller[A](implicit codec: JsonValueCodec[A]): ToEntityMarshaller[A] = {
    val ct = ContentType.WithFixedCharset(MediaTypes.`application/json`)
    Marshaller.withFixedContentType(ct)(a => HttpEntity.Strict(ct, ByteString.fromArrayUnsafe(writeToArray(a))))
  }
}
