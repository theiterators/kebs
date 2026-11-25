package pl.iterators.kebs.circe.formats

import cats.data.NonEmptyList
import io.circe.{Codec, Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.testkit.nested._

object CirceNestedInstancesTestDomain {
  def dataEncoder[A, P](items: P => List[A])(implicit a: Encoder[A]): Encoder[P] =
    Encoder.instance(p => Json.obj("DATA" -> Json.fromValues(items(p).map(a.apply))))
  def dataDecoder[A, P](page: List[A] => P)(implicit a: Decoder[A]): Decoder[P] =
    Decoder.instance(_.downField("DATA").as[List[A]].map(page))

  trait PageCodecs {
    implicit def pageEncoder[A](implicit a: Encoder[A]): Encoder[Page[A]]         = dataEncoder[A, Page[A]](_.items)
    implicit def pageDecoder[A](implicit a: Decoder[A]): Decoder[Page[A]]         = dataDecoder[A, Page[A]](Page(_))
    implicit def opaqueEncoder(implicit hidden: Encoder[Hidden]): Encoder[Opaque] =
      Encoder.instance(o => Json.obj("OPAQUE" -> hidden(Hidden(o.v, Item("h", o.w)))))
    implicit def opaqueDecoder(implicit hidden: Decoder[Hidden]): Decoder[Opaque] =
      Decoder.instance(_.downField("OPAQUE").as[Hidden].map(h => Opaque(h.k, h.i.n)))
  }
  object PageCodecs extends PageCodecs

  final case class Box[A](items: List[A])
  object Box {
    implicit def encoder[A](implicit a: Encoder[A]): Encoder[Box[A]] = dataEncoder[A, Box[A]](_.items)
    implicit def decoder[A](implicit a: Decoder[A]): Decoder[Box[A]] = dataDecoder[A, Box[A]](Box(_))
  }
  final case class RecBox(v: Int, kids: List[Box[RecBox]])
  final case class RecOptionBox(v: Int, o: Option[Box[RecOptionBox]])
  final case class RecTupleBox(v: Int, o: List[(Int, Box[RecTupleBox])])
  final case class BehindKnot(v: Int, kids: Box[BehindKnot])
  final case class Knot(v: Int, w: Int)
  object Knot {
    implicit def encoder(implicit box: Encoder[Box[BehindKnot]]): Encoder[Knot] =
      Encoder.instance(k => Json.obj("KNOT" -> box(Box(List(BehindKnot(k.v, Box(Nil)))))))
    implicit def decoder(implicit box: Decoder[Box[BehindKnot]]): Decoder[Knot] =
      Decoder.instance(_.downField("KNOT").as[Box[BehindKnot]].map(b => Knot(b.items.head.v, 2)))
  }
  final case class HasKnot(x: Knot, n: Int)

  final case class HasNonEmptyList(x: NonEmptyList[Item], n: Int)
  final case class RecNonEmptyList(v: Int, x: Option[NonEmptyList[RecNonEmptyList]])

  final case class Underivable(stream: java.io.InputStream)
  final case class Phantom[A](value: String)
  object Phantom {
    implicit def encoder[A]: Encoder[Phantom[A]] = Encoder.encodeString.contramap(_.value)
    implicit def decoder[A]: Decoder[Phantom[A]] = Decoder.decodeString.map(Phantom(_))
  }
  final case class HasPhantom(id: Phantom[Underivable], ids: List[Phantom[Underivable]])

  final case class FlatNode(edges: Map[String, FlatNode])
  final case class HasFlatNode(x: FlatNode, n: Int)

  // one instance for both directions, so each search sees the other direction's types too
  final case class Both[A](a: A)
  object Both {
    implicit def codec[A](implicit e: Encoder[A], d: Decoder[A]): Codec[Both[A]] =
      Codec.from(d.at("BOTH").map(Both(_)), Encoder.instance(b => Json.obj("BOTH" -> e(b.a))))
  }
  final case class RecBoth(v: Int, next: Option[Both[RecBoth]])
}

class CirceNestedInstancesTests extends AnyFunSuite with Matchers {
  import CirceNestedInstancesTestDomain._

  object Imported extends KebsCirce
  object MixedIn  extends KebsCirce with PageCodecs
  object Flat     extends KebsCirce with CaseClass1ToValueClass

  private def check[T](value: T, json: String)(implicit encoder: Encoder[T], decoder: Decoder[T]) = {
    encoder(value).noSpaces shouldBe json
    io.circe.parser.decode[T](json) shouldBe Right(value)
  }

  test("derived types inside the library's containers") {
    import Imported._
    check(HasList(List(item), 1), """{"x":[{"name":"i","n":1}],"n":1}""")
    check(HasOption(Some(item), 1), """{"x":{"name":"i","n":1},"n":1}""")
    check(HasMap(Map("k" -> item), 1), """{"x":{"k":{"name":"i","n":1}},"n":1}""")
    check(HasTuple((1, item), 1), """{"x":[1,{"name":"i","n":1}],"n":1}""")
    check(HasEither(Right(item), 1), """{"x":{"Right":{"value":{"name":"i","n":1}}},"n":1}""")
    check(HasNonEmptyList(NonEmptyList.of(item), 1), """{"x":[{"name":"i","n":1}],"n":1}""")
    check(HasGen(Gen(item, 2), 1), """{"x":{"x":{"name":"i","n":1},"n":2},"n":1}""")
    check(
      Diamond0(Diamond1(item, item), Diamond1(item, item)),
      """{"a":{"a":{"name":"i","n":1},"b":{"name":"i","n":1}},"b":{"a":{"name":"i","n":1},"b":{"name":"i","n":1}}}"""
    )
  }

  test("a flat wrapper of a derived class") {
    import Flat._
    check(HasWrapper(Wrapper(item), 1), """{"x":{"name":"i","n":1},"n":1}""")
    check(FlatTree(1, FlatChildren(List(FlatTree(2, FlatChildren(Nil))))), """{"value":1,"children":[{"value":2,"children":[]}]}""")
  }

  test("a user's generic codecs of derived and recursive types, imported") {
    import Imported._, PageCodecs._
    check(HasPage(Page(List(item)), 1), """{"x":{"DATA":[{"name":"i","n":1}]},"n":1}""")
    check(HasPages(Page(List(Page(List(item)))), 1), """{"x":{"DATA":[{"DATA":[{"name":"i","n":1}]}]},"n":1}""")
    check(Page(List(item)), """{"DATA":[{"name":"i","n":1}]}""")
    check(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    check(Page(List(recPage)), """{"DATA":[{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}]}""")
    check(mutA, """{"name":"a","b":{"as":{"DATA":[{"name":"b","b":null}]},"n":2}}""")
    check(adt, """{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}""")
    check(
      HasAdt(adt, 1),
      """{"x":{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}},"n":1}"""
    )
    check(HasOpaque(Opaque(1, 2), 1), """{"x":{"OPAQUE":{"k":1,"i":{"name":"h","n":2}}},"n":1}""")
  }

  test("a user's generic codecs, mixed into the protocol") {
    import MixedIn._
    check(HasPage(Page(List(item)), 1), """{"x":{"DATA":[{"name":"i","n":1}]},"n":1}""")
    check(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    check(List(mutA), """[{"name":"a","b":{"as":{"DATA":[{"name":"b","b":null}]},"n":2}}]""")
  }

  test("a user's generic codecs in the companion, requested through containers") {
    import Imported._
    val rec = RecBox(1, List(Box(List(RecBox(2, Nil)))))
    check(rec, """{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}""")
    check(List(Box(List(rec))), """[{"DATA":[{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}]}]""")
    val option = Box(List(RecOptionBox(1, Some(Box(List(RecOptionBox(2, None)))))))
    check(Option(option), """{"DATA":[{"v":1,"o":{"DATA":[{"v":2,"o":null}]}}]}""")
    check(List(Option(option)), """[{"DATA":[{"v":1,"o":{"DATA":[{"v":2,"o":null}]}}]}]""")
    check(List((1, Box(List(RecTupleBox(1, List((2, Box(Nil)))))))), """[[1,{"DATA":[{"v":1,"o":[[2,{"DATA":[]}]]}]}]]""")
  }

  test("a derived type hidden behind a user's codec") {
    import Imported._
    check(Knot(1, 2), """{"KNOT":{"DATA":[{"v":1,"kids":{"DATA":[]}}]}}""")
    check(HasKnot(Knot(1, 2), 1), """{"x":{"KNOT":{"DATA":[{"v":1,"kids":{"DATA":[]}}]}},"n":1}""")
  }

  test("a user's Codec that needs both directions of a recursive type") {
    import Imported._
    check(RecBoth(1, Some(Both(RecBoth(2, None)))), """{"v":1,"next":{"BOTH":{"v":2,"next":null}}}""")
    check(Both(RecBoth(1, None)), """{"BOTH":{"v":1,"next":null}}""")
  }

  test("recursion through the library's containers") {
    import Imported._
    check(RecOption(1, Some(RecOption(2, None))), """{"v":1,"next":{"v":2,"next":null}}""")
    check(RecList(1, List(RecList(2, Nil))), """{"v":1,"kids":[{"v":2,"kids":[]}]}""")
    check(RecMap(1, Map("k" -> RecMap(2, Map.empty))), """{"v":1,"kids":{"k":{"v":2,"kids":{}}}}""")
    check(RecTuple(1, List("k" -> RecTuple(2, Nil))), """{"v":1,"kids":[["k",{"v":2,"kids":[]}]]}""")
    check(
      RecEither(1, Right(RecEither(2, Left("end")))),
      """{"v":1,"next":{"Right":{"value":{"v":2,"next":{"Left":{"value":"end"}}}}}}"""
    )
    check(RecNonEmptyList(1, Some(NonEmptyList.of(RecNonEmptyList(2, None)))), """{"v":1,"x":[{"v":2,"x":null}]}""")
    check(NonEmptyList.of(RecNonEmptyList(2, None)), """[{"v":2,"x":null}]""")
    check(
      Map("k" -> List(RecMapOfLists(1, Map("j" -> List(RecMapOfLists(2, Map.empty)))))),
      """{"k":[{"v":1,"kids":{"j":[{"v":2,"kids":{}}]}}]}"""
    )
  }

  test("a local implicit for a nested type wins") {
    import Imported._, PageCodecs._
    implicit val itemEncoder: Encoder[Item] = Encoder.encodeString.contramap(i => s"${i.name}:${i.n}")
    Encoder[HasPage].apply(HasPage(Page(List(item)), 1)).noSpaces shouldBe """{"x":{"DATA":["i:1"]},"n":1}"""
    Encoder[HasList].apply(HasList(List(item), 1)).noSpaces shouldBe """{"x":["i:1"],"n":1}"""
  }

  test("nested types are derived with the protocol's config, read once per derivation") {
    var reads = 0
    object Upper extends KebsCirce with PageCodecs {
      override def kebsJsonConfig: JsonConfig = { reads += 1; super.kebsJsonConfig.withFieldNaming(NamingStyle.uppercase) }
    }
    import Upper._
    val encoder = Encoder[MutA]
    encoder(mutA).noSpaces shouldBe """{"NAME":"a","B":{"AS":{"DATA":[{"NAME":"b","B":null}]},"N":2}}"""
    reads shouldBe 1
  }

  test("a type argument that a codec does not need is not derived") {
    import Imported._
    check(HasPhantom(Phantom("a"), List(Phantom("b"))), """{"id":"a","ids":["b"]}""")
  }

  test("a flat wrapper of itself is reported, not derived as an object") {
    import Flat._
    "Encoder[HasFlatNode]" shouldNot typeCheck
    "Decoder[HasFlatNode]" shouldNot typeCheck
  }

  test("types that grow are reported") {
    import Imported._
    "Encoder[Grow[Int]]" shouldNot typeCheck
  }
}
