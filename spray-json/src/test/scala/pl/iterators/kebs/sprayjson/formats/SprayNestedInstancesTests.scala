package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.testkit.nested._
import spray.json._

object SprayNestedInstancesTestDomain {
  def dataFormat[A, P](items: P => List[A], page: List[A] => P)(implicit a: JsonFormat[A]): JsonFormat[P] = new JsonFormat[P] {
    def write(p: P): JsValue   = JsObject("DATA" -> JsArray(items(p).map(a.write).toVector))
    def read(json: JsValue): P = json match {
      case JsObject(fields) => page(fields("DATA").asInstanceOf[JsArray].elements.toList.map(a.read))
      case _                => deserializationError("DATA expected")
    }
  }

  trait PageFormats {
    implicit def pageFormat[A](implicit a: JsonFormat[A]): JsonFormat[Page[A]]         = dataFormat[A, Page[A]](_.items, Page(_))
    implicit def opaqueFormat(implicit hidden: JsonFormat[Hidden]): JsonFormat[Opaque] = new JsonFormat[Opaque] {
      def write(o: Opaque): JsValue   = JsObject("OPAQUE" -> hidden.write(Hidden(o.v, Item("h", o.w))))
      def read(json: JsValue): Opaque = {
        val h = hidden.read(json.asJsObject.fields("OPAQUE"))
        Opaque(h.k, h.i.n)
      }
    }
  }
  object PageFormats extends PageFormats

  final case class Box[A](items: List[A])
  object Box {
    implicit def format[A](implicit a: JsonFormat[A]): JsonFormat[Box[A]] = dataFormat[A, Box[A]](_.items, Box(_))
  }
  final case class RecBox(v: Int, kids: List[Box[RecBox]])
  final case class RecOptionBox(v: Int, o: Option[Box[RecOptionBox]])
  final case class RecTupleBox(v: Int, o: List[(Int, Box[RecTupleBox])])
  final case class BehindKnot(v: Int, kids: Box[BehindKnot])
  final case class Knot(v: Int, w: Int)
  object Knot {
    implicit def format(implicit box: JsonFormat[Box[BehindKnot]]): JsonFormat[Knot] = new JsonFormat[Knot] {
      def write(k: Knot): JsValue   = JsObject("KNOT" -> box.write(Box(List(BehindKnot(k.v, Box(Nil))))))
      def read(json: JsValue): Knot = Knot(box.read(json.asJsObject.fields("KNOT")).items.head.v, 2)
    }
  }
  final case class HasKnot(x: Knot, n: Int)

  final case class Underivable(stream: java.io.InputStream)
  final case class Phantom[A](value: String)
  object Phantom {
    implicit def format[A]: JsonFormat[Phantom[A]] = new JsonFormat[Phantom[A]] {
      def write(p: Phantom[A]): JsValue   = JsString(p.value)
      def read(json: JsValue): Phantom[A] = Phantom(json.asInstanceOf[JsString].value)
    }
  }
  final case class HasPhantom(id: Phantom[Underivable], ids: List[Phantom[Underivable]])

  final case class FlatNode(edges: Map[String, FlatNode])
  final case class HasFlatNode(x: FlatNode, n: Int)

  final case class Envelope[A](a: A, n: Int)
  object Envelope {
    implicit def format[A](implicit a: RootJsonFormat[A]): RootJsonFormat[Envelope[A]] = new RootJsonFormat[Envelope[A]] {
      def write(e: Envelope[A]): JsValue   = JsObject("ENVELOPE" -> a.write(e.a))
      def read(json: JsValue): Envelope[A] = Envelope(a.read(json.asJsObject.fields("ENVELOPE")), 1)
    }
  }
  final case class Coded(v: Int, w: Int)
  object Coded {
    implicit val format: JsonFormat[Coded] = new JsonFormat[Coded] {
      def write(c: Coded): JsValue   = JsString(s"${c.v}/${c.w}")
      def read(json: JsValue): Coded = Coded(1, 2)
    }
  }
  final case class HasEnvelope(x: Envelope[Item], n: Int)
  final case class HasCodedEnvelope(x: Envelope[Coded], n: Int)

  object Derived extends DefaultJsonProtocol with KebsSprayJson
  object Defined extends PageFormats with DefaultJsonProtocol {
    implicit val recFormat: RootJsonFormat[RecPage] = Derived.deriveFormat[RecPage]
    implicit val adtFormat: RootJsonFormat[Adt]     = Derived.deriveFormat[Adt]
  }
}

class SprayNestedInstancesTests extends AnyFunSuite with Matchers {
  import SprayNestedInstancesTestDomain._

  object Imported extends DefaultJsonProtocol with KebsSprayJson
  object MixedIn  extends DefaultJsonProtocol with KebsSprayJson with PageFormats
  object Flat     extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass

  private def check[T](value: T, json: String)(implicit format: JsonFormat[T]) = {
    format.write(value).compactPrint shouldBe json
    format.read(json.parseJson) shouldBe value
  }

  private def checkRoot[T](value: T, json: String)(implicit format: RootJsonFormat[T]) = check(value, json)

  test("derived types inside the library's containers") {
    import Imported._
    checkRoot(HasList(List(item), 1), """{"n":1,"x":[{"n":1,"name":"i"}]}""")
    checkRoot(HasOption(Some(item), 1), """{"n":1,"x":{"n":1,"name":"i"}}""")
    checkRoot(HasMap(Map("k" -> item), 1), """{"n":1,"x":{"k":{"n":1,"name":"i"}}}""")
    checkRoot(HasTuple((1, item), 1), """{"n":1,"x":[1,{"n":1,"name":"i"}]}""")
    checkRoot(HasEither(Right(item), 1), """{"n":1,"x":{"n":1,"name":"i"}}""")
    checkRoot(HasGen(Gen(item, 2), 1), """{"n":1,"x":{"n":2,"x":{"n":1,"name":"i"}}}""")
    checkRoot(Gen(item, 2), """{"n":2,"x":{"n":1,"name":"i"}}""")
    checkRoot(
      Diamond0(Diamond1(item, item), Diamond1(item, item)),
      """{"a":{"a":{"n":1,"name":"i"},"b":{"n":1,"name":"i"}},"b":{"a":{"n":1,"name":"i"},"b":{"n":1,"name":"i"}}}"""
    )
  }

  test("a flat wrapper of a derived class") {
    import Flat._
    checkRoot(HasWrapper(Wrapper(item), 1), """{"n":1,"x":{"n":1,"name":"i"}}""")
    checkRoot(FlatTree(1, FlatChildren(List(FlatTree(2, FlatChildren(Nil))))), """{"children":[{"children":[],"value":2}],"value":1}""")
  }

  test("a user's generic format of derived and recursive types, imported") {
    import Imported._, PageFormats._
    checkRoot(HasPage(Page(List(item)), 1), """{"n":1,"x":{"DATA":[{"n":1,"name":"i"}]}}""")
    checkRoot(HasPages(Page(List(Page(List(item)))), 1), """{"n":1,"x":{"DATA":[{"DATA":[{"n":1,"name":"i"}]}]}}""")
    checkRoot(Page(List(item)), """{"DATA":[{"n":1,"name":"i"}]}""")
    checkRoot(recPage, """{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}""")
    checkRoot(mutA, """{"b":{"as":{"DATA":[{"name":"b"}]},"n":2},"name":"a"}""")
    checkRoot(adt, """{"Branch":{"l":{"DATA":[{"Leaf":{"i":{"n":1,"name":"i"},"n":1}},{"Empty":{}}]},"r":[{"Empty":{}}]}}""")
    checkRoot(
      HasAdt(adt, 1),
      """{"n":1,"x":{"Branch":{"l":{"DATA":[{"Leaf":{"i":{"n":1,"name":"i"},"n":1}},{"Empty":{}}]},"r":[{"Empty":{}}]}}}"""
    )
    checkRoot(HasOpaque(Opaque(1, 2), 1), """{"n":1,"x":{"OPAQUE":{"i":{"n":2,"name":"h"},"k":1}}}""")
  }

  test("a user's generic format, mixed into the protocol") {
    import MixedIn._
    check(HasPage(Page(List(item)), 1), """{"n":1,"x":{"DATA":[{"n":1,"name":"i"}]}}""")
    check(recPage, """{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}""")
    checkRoot(Page(List(recPage)), """{"DATA":[{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}]}""")
    check(List(mutA), """[{"b":{"as":{"DATA":[{"name":"b"}]},"n":2},"name":"a"}]""")
  }

  test("a user's generic format in the companion, requested through containers") {
    import Imported._
    val rec = RecBox(1, List(Box(List(RecBox(2, Nil)))))
    check(rec, """{"kids":[{"DATA":[{"kids":[],"v":2}]}],"v":1}""")
    check(List(Box(List(rec))), """[{"DATA":[{"kids":[{"DATA":[{"kids":[],"v":2}]}],"v":1}]}]""")
    val option = Box(List(RecOptionBox(1, Some(Box(List(RecOptionBox(2, None)))))))
    check(Option(option), """{"DATA":[{"o":{"DATA":[{"v":2}]},"v":1}]}""")
    check(List(Option(option)), """[{"DATA":[{"o":{"DATA":[{"v":2}]},"v":1}]}]""")
    check(List((1, Box(List(RecTupleBox(1, List((2, Box(Nil)))))))), """[[1,{"DATA":[{"o":[[2,{"DATA":[]}]],"v":1}]}]]""")
  }

  test("a derived type hidden behind a user's format") {
    import Imported._
    check(Knot(1, 2), """{"KNOT":{"DATA":[{"kids":{"DATA":[]},"v":1}]}}""")
    check(HasKnot(Knot(1, 2), 1), """{"n":1,"x":{"KNOT":{"DATA":[{"kids":{"DATA":[]},"v":1}]}}}""")
  }

  test("recursion through the library's containers") {
    import Imported._
    checkRoot(RecOption(1, Some(RecOption(2, None))), """{"next":{"v":2},"v":1}""")
    checkRoot(RecList(1, List(RecList(2, Nil))), """{"kids":[{"kids":[],"v":2}],"v":1}""")
    checkRoot(RecMap(1, Map("k" -> RecMap(2, Map.empty))), """{"kids":{"k":{"kids":{},"v":2}},"v":1}""")
    checkRoot(RecTuple(1, List("k" -> RecTuple(2, Nil))), """{"kids":[["k",{"kids":[],"v":2}]],"v":1}""")
    checkRoot(RecEither(1, Right(RecEither(2, Left("end")))), """{"next":{"next":"end","v":2},"v":1}""")
    check(
      Map("k" -> List(RecMapOfLists(1, Map("j" -> List(RecMapOfLists(2, Map.empty)))))),
      """{"k":[{"kids":{"j":[{"kids":{},"v":2}]},"v":1}]}"""
    )
  }

  test("a local implicit for a nested type wins") {
    import Imported._, PageFormats._
    implicit val itemFormat: JsonFormat[Item] = new JsonFormat[Item] {
      def write(i: Item): JsValue   = JsString(s"${i.name}:${i.n}")
      def read(json: JsValue): Item = item
    }
    check(HasPage(Page(List(item)), 1), """{"n":1,"x":{"DATA":["i:1"]}}""")
    checkRoot(HasList(List(item), 1), """{"n":1,"x":["i:1"]}""")
    deriveFormat[HasTuple].write(HasTuple((1, item), 1)).compactPrint shouldBe """{"n":1,"x":[1,"i:1"]}"""
  }

  test("an implicit defined by an explicit derivation of a recursive type") {
    import Defined._
    check(recPage, """{"kids":{"DATA":[{"kids":{"DATA":[]},"v":2}]},"v":1}""")
    check(List(adt), """[{"Branch":{"l":{"DATA":[{"Leaf":{"i":{"n":1,"name":"i"},"n":1}},{"Empty":{}}]},"r":[{"Empty":{}}]}}]""")
  }

  test("nested types are derived with the protocol's config, read once per derivation") {
    var reads = 0
    object Upper extends DefaultJsonProtocol with KebsSprayJson with PageFormats {
      override def kebsJsonConfig: JsonConfig = { reads += 1; super.kebsJsonConfig.withFieldNaming(NamingStyle.uppercase) }
    }
    import Upper._
    val format = implicitly[RootJsonFormat[MutA]]
    format.write(mutA).compactPrint shouldBe """{"B":{"AS":{"DATA":[{"NAME":"b"}]},"N":2},"NAME":"a"}"""
    reads shouldBe 1
  }

  test("a type argument that a user's format does not need is not derived") {
    import Imported._
    check(HasPhantom(Phantom("a"), List(Phantom("b"))), """{"id":"a","ids":["b"]}""")
  }

  test("a user's format that needs a RootJsonFormat") {
    import Imported._
    check(HasEnvelope(Envelope(item, 1), 1), """{"n":1,"x":{"ENVELOPE":{"n":1,"name":"i"}}}""")
    // Coded has only a JsonFormat: deriving Envelope instead would ignore its format without a word
    "implicitly[RootJsonFormat[HasCodedEnvelope]]" shouldNot typeCheck
  }

  test("a flat wrapper of itself is reported, not derived as an object") {
    import Flat._
    "implicitly[RootJsonFormat[HasFlatNode]]" shouldNot typeCheck
  }

  test("types that grow are reported") {
    import Imported._
    "implicitly[RootJsonFormat[Grow[Int]]]" shouldNot typeCheck
  }
}
