package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.jsoniter.KebsJsoniter

// the types of the codecs are inferred, so the compiler types their derivation when a search in the other file, of either
// name and compiled in either order, meets them
object JsoniterAaCompanion {
  object Protocol extends KebsJsoniter
  import Protocol._

  final case class Inner(x: Int, y: Int)
  final case class Outer(inner: Inner, n: Int)
  object Outer {
    implicit val codec =
      JsoniterNestedInstancesTestDomain.named("OUTER", deriveCodec[(Inner, Int)])((Outer.apply _).tupled, (o: Outer) => (o.inner, o.n))
  }

  final case class Named(someField: Int, otherField: Int)
  object Named { implicit val codec = pl.iterators.kebs.jsoniter.snakified.deriveCodec[Named] }
  final case class NamedHolder(named: JsoniterZzCompanion.Named, n: Int)
  def writtenNamed: String = writeToString(NamedHolder(JsoniterZzCompanion.Named(1, 2), 3))

  final case class Holder(outer: JsoniterZzCompanion.Outer, n: Int)
  def written: String = writeToString(Holder(JsoniterZzCompanion.Outer(JsoniterZzCompanion.Inner(1, 2), 3), 4))
  def read: Holder    = readFromString[Holder](written)
}
