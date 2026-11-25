package pl.iterators.kebs.playjson.formats

import pl.iterators.kebs.playjson.KebsPlayJson
import play.api.libs.json._

// the type of `format` is inferred, so the compiler types its derivation when a search in the other file, of either name
// and compiled in either order, meets it
object PlayZzCompanion {
  object Protocol extends KebsPlayJson
  import Protocol._

  final case class Inner(x: Int, y: Int)
  final case class Outer(inner: Inner, n: Int)
  object Outer {
    implicit val format = Json.format[Outer]
  }

  final case class Holder(outer: PlayAaCompanion.Outer, n: Int)
  def written: String = Json.stringify(Json.toJson(Holder(PlayAaCompanion.Outer(PlayAaCompanion.Inner(1, 2), 3), 4)))
}
