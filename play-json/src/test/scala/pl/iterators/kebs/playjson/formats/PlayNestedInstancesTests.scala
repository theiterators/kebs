package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.nested._
import play.api.libs.json._

object PlayNestedInstancesTestDomain {
  def dataReads[A, P](page: List[A] => P)(implicit a: Reads[A]): Reads[P]      = Reads(json => (json \ "DATA").validate[List[A]].map(page))
  def dataWrites[A, P](items: P => List[A])(implicit a: Writes[A]): OWrites[P] =
    OWrites(p => JsObject(Seq("DATA" -> JsArray(items(p).map(a.writes)))))

  trait PageInstances {
    implicit def pageReads[A](implicit a: Reads[A]): Reads[Page[A]]         = dataReads[A, Page[A]](Page(_))
    implicit def pageWrites[A](implicit a: Writes[A]): Writes[Page[A]]      = dataWrites[A, Page[A]](_.items)
    implicit def opaqueReads(implicit hidden: Reads[Hidden]): Reads[Opaque] =
      Reads(json => (json \ "OPAQUE").validate[Hidden].map(h => Opaque(h.k, h.i.n)))
    implicit def opaqueWrites(implicit hidden: Writes[Hidden]): Writes[Opaque] =
      OWrites(o => JsObject(Seq("OPAQUE" -> hidden.writes(Hidden(o.v, Item("h", o.w))))))
  }
  object PageInstances extends PageInstances

  final case class Box[A](items: List[A])
  object Box {
    implicit def reads[A](implicit a: Reads[A]): Reads[Box[A]]    = dataReads[A, Box[A]](Box(_))
    implicit def writes[A](implicit a: Writes[A]): Writes[Box[A]] = dataWrites[A, Box[A]](_.items)
  }
  final case class RecBox(v: Int, kids: List[Box[RecBox]])
  final case class RecOptionBox(v: Int, o: Option[Box[RecOptionBox]])
  final case class RecTupleBox(v: Int, o: List[(Int, Box[RecTupleBox])])
  final case class BehindKnot(v: Int, kids: Box[BehindKnot])
  final case class Knot(v: Int, w: Int)
  object Knot {
    implicit def reads(implicit box: Reads[Box[BehindKnot]]): Reads[Knot] =
      Reads(json => (json \ "KNOT").validate[Box[BehindKnot]].map(b => Knot(b.items.head.v, 2)))
    implicit def writes(implicit box: Writes[Box[BehindKnot]]): Writes[Knot] =
      OWrites(k => JsObject(Seq("KNOT" -> box.writes(Box(List(BehindKnot(k.v, Box(Nil))))))))
  }
  final case class HasKnot(x: Knot, n: Int)

  final case class Underivable(stream: java.io.InputStream)
  final case class Phantom[A](value: String)
  object Phantom {
    implicit def reads[A]: Reads[Phantom[A]]   = Reads(json => json.validate[String].map(Phantom(_)))
    implicit def writes[A]: Writes[Phantom[A]] = Writes(p => JsString(p.value))
  }
  final case class HasPhantom(id: Phantom[Underivable], ids: List[Phantom[Underivable]])
  final case class MapKey(value: java.io.InputStream)
  final case class KeyedByUnderivable(m: Map[MapKey, Item])
  object MapKey {
    implicit val keyWrites: KeyWrites[MapKey] = KeyWrites(_ => "key")
  }

  final case class FlatNode(edges: Map[String, FlatNode])
  final case class HasFlatNode(x: FlatNode, n: Int)

  final case class Envelope[A](a: A, n: Int)
  object Envelope {
    implicit def writes[A](implicit a: OWrites[A]): OWrites[Envelope[A]] = OWrites(e => JsObject(Seq("ENVELOPE" -> a.writes(e.a))))
  }
  final case class Coded(v: Int, w: Int)
  object Coded {
    implicit val writes: Writes[Coded] = Writes(c => JsString(s"${c.v}/${c.w}"))
  }
  final case class HasEnvelope(x: Envelope[Item], n: Int)
  final case class HasCodedEnvelope(x: Envelope[Coded], n: Int)

  // one instance for both directions, so each search sees the other direction's types too
  final case class Both[A](a: A)
  object Both {
    implicit def format[A](implicit r: Reads[A], w: Writes[A]): Format[Both[A]] =
      Format(Reads(json => (json \ "BOTH").validate[A].map(Both(_))), Writes(b => JsObject(Seq("BOTH" -> w.writes(b.a)))))
  }
  final case class RecBoth(v: Int, next: Option[Both[RecBoth]])

  object Derived extends KebsPlayJson
  object Defined extends PageInstances {
    implicit val recFormat: OFormat[RecPage] = Derived.deriveFormat[RecPage]
    implicit val adtFormat: OFormat[Adt]     = Derived.deriveFormat[Adt]
  }
}

class PlayNestedInstancesTests extends AnyFunSuite with Matchers {
  import PlayNestedInstancesTestDomain._

  object Imported extends KebsPlayJson
  object MixedIn  extends KebsPlayJson with PageInstances
  object Flat     extends KebsPlayJson with CaseClass1ToValueClass

  private def check[T](value: T, json: String)(implicit writes: Writes[T], reads: Reads[T]) = {
    Json.stringify(writes.writes(value)) shouldBe json
    reads.reads(Json.parse(json)) shouldBe JsSuccess(value)
  }

  test("derived types inside the library's containers") {
    import Imported._
    check(HasList(List(item), 1), """{"x":[{"name":"i","n":1}],"n":1}""")
    check(HasOption(Some(item), 1), """{"x":{"name":"i","n":1},"n":1}""")
    check(HasMap(Map("k" -> item), 1), """{"x":{"k":{"name":"i","n":1}},"n":1}""")
    check(HasTuple((1, item), 1), """{"x":[1,{"name":"i","n":1}],"n":1}""")
    check(HasGen(Gen(item, 2), 1), """{"x":{"x":{"name":"i","n":1},"n":2},"n":1}""")
    check(
      Diamond0(Diamond1(item, item), Diamond1(item, item)),
      """{"a":{"a":{"name":"i","n":1},"b":{"name":"i","n":1}},"b":{"a":{"name":"i","n":1},"b":{"name":"i","n":1}}}"""
    )
    "implicitly[Writes[HasEither]]" shouldNot typeCheck
  }

  test("a flat wrapper of a derived class") {
    import Flat._
    check(HasWrapper(Wrapper(item), 1), """{"x":{"name":"i","n":1},"n":1}""")
    check(FlatTree(1, FlatChildren(List(FlatTree(2, FlatChildren(Nil))))), """{"value":1,"children":[{"value":2,"children":[]}]}""")
  }

  test("a user's generic instances of derived and recursive types, imported") {
    import Imported._, PageInstances._
    check(HasPage(Page(List(item)), 1), """{"x":{"DATA":[{"name":"i","n":1}]},"n":1}""")
    check(HasPages(Page(List(Page(List(item)))), 1), """{"x":{"DATA":[{"DATA":[{"name":"i","n":1}]}]},"n":1}""")
    check(Page(List(item)), """{"DATA":[{"name":"i","n":1}]}""")
    check(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    check(Page(List(recPage)), """{"DATA":[{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}]}""")
    check(mutA, """{"name":"a","b":{"as":{"DATA":[{"name":"b"}]},"n":2}}""")
    check(adt, """{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}""")
    check(
      HasAdt(adt, 1),
      """{"x":{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}},"n":1}"""
    )
    check(HasOpaque(Opaque(1, 2), 1), """{"x":{"OPAQUE":{"k":1,"i":{"name":"h","n":2}}},"n":1}""")
  }

  test("a user's generic instances, mixed into the protocol") {
    import MixedIn._
    check(HasPage(Page(List(item)), 1), """{"x":{"DATA":[{"name":"i","n":1}]},"n":1}""")
    check(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    check(List(mutA), """[{"name":"a","b":{"as":{"DATA":[{"name":"b"}]},"n":2}}]""")
  }

  test("a user's generic instances in the companion, requested through containers") {
    import Imported._
    val rec = RecBox(1, List(Box(List(RecBox(2, Nil)))))
    check(rec, """{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}""")
    check(List(Box(List(rec))), """[{"DATA":[{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}]}]""")
    val option = Box(List(RecOptionBox(1, Some(Box(List(RecOptionBox(2, None)))))))
    check(List(option), """[{"DATA":[{"v":1,"o":{"DATA":[{"v":2}]}}]}]""")
    Json.stringify(Json.toJson(Option(option))) shouldBe """{"DATA":[{"v":1,"o":{"DATA":[{"v":2}]}}]}"""
    check(List((1, Box(List(RecTupleBox(1, List((2, Box(Nil)))))))), """[[1,{"DATA":[{"v":1,"o":[[2,{"DATA":[]}]]}]}]]""")
  }

  test("a derived type hidden behind a user's instance") {
    import Imported._
    check(Knot(1, 2), """{"KNOT":{"DATA":[{"v":1,"kids":{"DATA":[]}}]}}""")
    check(HasKnot(Knot(1, 2), 1), """{"x":{"KNOT":{"DATA":[{"v":1,"kids":{"DATA":[]}}]}},"n":1}""")
  }

  test("a user's Format that needs both directions of a recursive type") {
    import Imported._
    check(RecBoth(1, Some(Both(RecBoth(2, None)))), """{"v":1,"next":{"BOTH":{"v":2}}}""")
    check(Both(RecBoth(1, None)), """{"BOTH":{"v":1}}""")
  }

  test("recursion through the library's containers") {
    import Imported._
    check(RecOption(1, Some(RecOption(2, None))), """{"v":1,"next":{"v":2}}""")
    check(RecList(1, List(RecList(2, Nil))), """{"v":1,"kids":[{"v":2,"kids":[]}]}""")
    check(RecMap(1, Map("k" -> RecMap(2, Map.empty))), """{"v":1,"kids":{"k":{"v":2,"kids":{}}}}""")
    check(RecTuple(1, List("k" -> RecTuple(2, Nil))), """{"v":1,"kids":[["k",{"v":2,"kids":[]}]]}""")
    check(
      Map("k" -> List(RecMapOfLists(1, Map("j" -> List(RecMapOfLists(2, Map.empty)))))),
      """{"k":[{"v":1,"kids":{"j":[{"v":2,"kids":{}}]}}]}"""
    )
  }

  test("a local implicit for a nested type wins") {
    import Imported._, PageInstances._
    implicit val itemWrites: Writes[Item] = Writes(i => JsString(s"${i.name}:${i.n}"))
    Json.stringify(Json.toJson(HasPage(Page(List(item)), 1))) shouldBe """{"x":{"DATA":["i:1"]},"n":1}"""
    Json.stringify(Json.toJson(HasList(List(item), 1))) shouldBe """{"x":["i:1"],"n":1}"""
    Json.stringify(deriveWrites[HasTuple].writes(HasTuple((1, item), 1))) shouldBe """{"x":[1,"i:1"],"n":1}"""
  }

  test("an implicit defined by an explicit derivation of a recursive type") {
    import Imported._, Defined._
    check(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    check(List(adt), """[{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}]""")
  }

  test("nested types are derived with the protocol's config, read once per derivation") {
    var reads = 0
    object Upper extends KebsPlayJson with PageInstances {
      override def kebsJsonConfig: JsonConfig = { reads += 1; super.kebsJsonConfig.withFieldNaming(NamingStyle.uppercase) }
    }
    import Upper._
    val writes = implicitly[OWrites[MutA]]
    Json.stringify(writes.writes(mutA)) shouldBe """{"NAME":"a","B":{"AS":{"DATA":[{"NAME":"b"}]},"N":2}}"""
    reads shouldBe 1
  }

  test("a type argument that an instance does not need is not derived") {
    import Imported._
    check(HasPhantom(Phantom("a"), List(Phantom("b"))), """{"id":"a","ids":["b"]}""")
    Json.stringify(Json.toJson(KeyedByUnderivable(Map(MapKey(null) -> item)))) shouldBe """{"m":{"key":{"name":"i","n":1}}}"""
  }

  test("a user's instance that needs an OWrites") {
    import Imported._
    Json.stringify(Json.toJson(HasEnvelope(Envelope(item, 1), 1))) shouldBe """{"x":{"ENVELOPE":{"name":"i","n":1}},"n":1}"""
    // Coded has only a Writes: deriving Envelope instead would ignore its instance without a word
    "implicitly[Writes[HasCodedEnvelope]]" shouldNot typeCheck
  }

  test("a flat wrapper of itself is reported, not derived as an object") {
    import Flat._
    "implicitly[Writes[HasFlatNode]]" shouldNot typeCheck
    "implicitly[Reads[HasFlatNode]]" shouldNot typeCheck
  }

  test("types that grow are reported") {
    import Imported._
    "implicitly[Writes[Grow[Int]]]" shouldNot typeCheck
  }
}
