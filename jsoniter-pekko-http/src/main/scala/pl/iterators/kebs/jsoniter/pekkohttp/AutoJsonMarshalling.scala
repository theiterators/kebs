package pl.iterators.kebs.jsoniter.pekkohttp

import com.github.pjfanning.pekkohttpjsoniterscala.JsoniterScalaSupport
import com.github.plokhotnyuk.jsoniter_scala.core._
import org.apache.pekko.http.scaladsl.marshalling.{Marshaller, PredefinedToResponseMarshallers}
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller
import pl.iterators.kebs.jsoniter.KebsJsoniter

import scala.annotation.nowarn
import scala.compiletime.summonFrom

object AutoJsonMarshalling {

  /** `Marshaller[-A, +B]` is contravariant in A, and Scala 3 maximizes an unpinned contravariant type variable to `Any`. The structural
    * refinement `{ type Pin = A }` adds an INVARIANT occurrence of A to the declared result type, which forces the compiler to pin A to
    * the exact expected type instead. `Marshaller` is sealed, hence refinement + cast (type-level only) rather than a subclass.
    */
  type PinnedEntityMarshaller[A]    = Marshaller[A, MessageEntity] { type Pin = A }
  type PinnedStatusMarshaller[S, A] = Marshaller[(S, A), HttpResponse] { type Pin = (S, A) }
  type PinnedEntityUnmarshaller[A]  = Unmarshaller[HttpEntity, A] { type Pin = A }
}

/** OPT-IN "mix in and everything marshals" ergonomics for pekko-http.
  *
  * Codecs come from an existing implicit `JsonValueCodec[A]` when one is in scope (e.g. a companion-object codec) and are derived
  * kebs-aware via `deriveCodec` otherwise.
  *
  * ==Sharp edges — read before mixing in==
  *   - every pekko marshallable shape needs a pinned instance here; shapes not covered (e.g. `(StatusCode, headers, T)` triples, streaming
  *     Sources) fall back to pekko's contravariant implicits and die with the 'scala.Any' error;
  *   - types that already have non-JSON marshallers get hijacked: `complete(OK -> "hello")` becomes `application/json` `"hello"` instead
  *     of `text/plain` hello;
  *   - types jsoniter cannot derive fail with a hard macro error even though pekko has a marshaller for them:
  *     `complete(OK -> HttpEntity("raw"))` stops compiling in scopes that mix this trait in.
  */
trait AutoJsonMarshalling {
  this: KebsJsoniter & JsoniterScalaSupport =>

  import AutoJsonMarshalling._

  @nowarn("msg=unused")
  private inline def codecOf[A]: JsonValueCodec[A] =
    summonFrom {
      case c: JsonValueCodec[A] => c // respect explicit companion codecs: one codec class, one wire format
      case _                    => deriveCodec[A]
    }

  // NOTE: the codec is passed explicitly rather than bound to a local implicit val —
  // a local `implicit val codec: JsonValueCodec[A] = codecOf[A]` would be found by
  // codecOf's own summonFrom, producing a self-referential codec (runtime infinite loop)

  inline implicit def autoEntityMarshaller[A]: PinnedEntityMarshaller[A] =
    marshaller[A](using codecOf[A]).asInstanceOf[PinnedEntityMarshaller[A]]

  inline implicit def autoStatusMarshaller[S, A](implicit sConv: S => StatusCode): PinnedStatusMarshaller[S, A] =
    PredefinedToResponseMarshallers
      .fromStatusCodeAndValue[S, A](using sConv, marshaller[A](using codecOf[A]))
      .asInstanceOf[PinnedStatusMarshaller[S, A]]

  inline implicit def autoEntityUnmarshaller[A]: PinnedEntityUnmarshaller[A] =
    unmarshaller[A](using codecOf[A]).asInstanceOf[PinnedEntityUnmarshaller[A]]
}
