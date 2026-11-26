package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder}
import pl.iterators.kebs.circe.KebsCirce

// the types of the codecs are inferred, so the compiler types their derivation when a search in the other file, of either
// name and compiled in either order, meets them
object CirceZzCompanion {
  object Protocol extends KebsCirce
  import Protocol._

  final case class Inner(x: Int, y: Int)
  final case class Outer(inner: Inner, n: Int)
  object Outer {
    implicit val encoder = Encoder.forProduct2("INNER", "n")((outer: Outer) => (outer.inner, outer.n))
    implicit val decoder = Decoder.forProduct2("INNER", "n")(Outer.apply)
  }

  final case class Holder(outer: CirceAaCompanion.Outer, n: Int)
  def written: String = Encoder[Holder].apply(Holder(CirceAaCompanion.Outer(CirceAaCompanion.Inner(1, 2), 3), 4)).noSpaces
  def read: Boolean   = io.circe.parser.decode[Holder](written).isRight
}
