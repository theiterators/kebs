package pl.iterators.kebs.sprayjson.formats

import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json._

// the type of `format` is inferred, so the compiler types its derivation when a search in the other file, of either name
// and compiled in either order, meets it
object SprayZzCompanion {
  object Protocol extends DefaultJsonProtocol with KebsSprayJson
  import Protocol._

  final case class Inner(x: Int, y: Int)
  final case class Outer(inner: Inner, n: Int)
  object Outer {
    implicit val format = jsonFormat2(Outer.apply)
  }

  final case class Holder(outer: SprayAaCompanion.Outer, n: Int)
  def written: String = Holder(SprayAaCompanion.Outer(SprayAaCompanion.Inner(1, 2), 3), 4).toJson.compactPrint
}
