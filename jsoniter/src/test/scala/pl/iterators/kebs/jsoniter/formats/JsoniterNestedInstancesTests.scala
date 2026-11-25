package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.nested._

object JsoniterNestedInstancesTestDomain {
  def named[A, B](name: String, codec: JsonValueCodec[A])(to: A => B, from: B => A): JsonValueCodec[B] = new JsonValueCodec[B] {
    def decodeValue(in: JsonReader, default: B): B = {
      if (!in.isNextToken('{') || in.readKeyAsString() != name) in.decodeError(s"expected $name")
      val a = codec.decodeValue(in, codec.nullValue)
      if (!in.isNextToken('}')) in.objectEndOrCommaError()
      to(a)
    }
    def encodeValue(x: B, out: JsonWriter): Unit = {
      out.writeObjectStart()
      out.writeKey(name)
      codec.encodeValue(from(x), out)
      out.writeObjectEnd()
    }
    def nullValue: B = null.asInstanceOf[B]
  }

  def list[A](a: JsonValueCodec[A]): JsonValueCodec[List[A]] = new JsonValueCodec[List[A]] {
    def decodeValue(in: JsonReader, default: List[A]): List[A] = {
      if (!in.isNextToken('[')) in.decodeError("expected [")
      if (in.isNextToken(']')) Nil
      else {
        in.rollbackToken()
        val builder = List.newBuilder[A]
        builder += a.decodeValue(in, a.nullValue)
        while (in.isNextToken(',')) builder += a.decodeValue(in, a.nullValue)
        builder.result()
      }
    }
    def encodeValue(x: List[A], out: JsonWriter): Unit = {
      out.writeArrayStart()
      x.foreach(a.encodeValue(_, out))
      out.writeArrayEnd()
    }
    def nullValue: List[A] = Nil
  }

  def asString[A](to: String => A, from: A => String): JsonValueCodec[A] = new JsonValueCodec[A] {
    def decodeValue(in: JsonReader, default: A): A = to(in.readString(null))
    def encodeValue(x: A, out: JsonWriter): Unit   = out.writeVal(from(x))
    def nullValue: A                               = null.asInstanceOf[A]
  }

  trait PageCodecs {
    implicit def page[A](implicit a: JsonValueCodec[A]): JsonValueCodec[Page[A]]         = named("DATA", list(a))(Page(_), _.items)
    implicit def opaque(implicit hidden: JsonValueCodec[Hidden]): JsonValueCodec[Opaque] =
      named("OPAQUE", hidden)(h => Opaque(h.k, h.i.n), o => Hidden(o.v, Item("h", o.w)))
  }
  object PageCodecs extends PageCodecs

  final case class Box[A](items: List[A])
  object Box { implicit def codec[A](implicit a: JsonValueCodec[A]): JsonValueCodec[Box[A]] = named("DATA", list(a))(Box(_), _.items) }
  final case class RecBox(v: Int, kids: List[Box[RecBox]])
  final case class RecOptionBox(v: Int, o: Option[Box[RecOptionBox]])
  final case class RecTupleBox(v: Int, o: List[(Int, Box[RecTupleBox])])

  // needs codecs that kebs does not offer on its own
  final case class Listed[A](items: List[A])
  object Listed {
    implicit def codec[A](implicit items: JsonValueCodec[List[A]]): JsonValueCodec[Listed[A]] = named("LISTED", items)(Listed(_), _.items)
  }
  final case class Maybe[A](value: Option[A], count: Int)
  object Maybe {
    implicit def codec[A](implicit both: JsonValueCodec[(Option[A], Int)]): JsonValueCodec[Maybe[A]] =
      named("MAYBE", both)(b => Maybe(b._1, b._2), m => (m.value, m.count))
  }
  final case class HasBoxes(
      ints: Box[Int],
      strings: Box[String],
      nested: Box[Box[Boolean]],
      listed: Listed[Item],
      numbers: Listed[Long],
      maybe: Maybe[Item]
  )
  final case class RecListed(v: Int, kids: Listed[RecListed])

  final case class BehindKnot(v: Int, kids: Box[BehindKnot])
  final case class Knot(v: Int, w: Int)
  object Knot {
    implicit def codec(implicit box: JsonValueCodec[Box[BehindKnot]]): JsonValueCodec[Knot] =
      named("KNOT", box)(b => Knot(b.items.head.v, 2), k => Box(List(BehindKnot(k.v, Box(Nil)))))
  }
  final case class HasKnot(x: Knot, n: Int)

  final case class Underivable(stream: java.io.InputStream)
  final case class Phantom[A](value: String)
  object Phantom { implicit def codec[A]: JsonValueCodec[Phantom[A]] = asString(Phantom(_), _.value) }
  final case class HasPhantom(id: Phantom[Underivable], ids: List[Phantom[Underivable]])

  final case class Point(x: Int, y: Int)
  object Point {
    implicit val codec: JsonValueCodec[Point] =
      asString(s => Point(s.takeWhile(_ != ',').toInt, s.dropWhile(_ != ',').drop(1).toInt), p => s"${p.x},${p.y}")
  }
  final case class Located(name: String, at: Point, others: List[Point])

  final case class RecOwn(v: Int, kids: List[Box[RecOwn]])
  object RecOwn { implicit val codec: JsonValueCodec[RecOwn] = pl.iterators.kebs.jsoniter.deriveCodec[RecOwn] }
}

class JsoniterNestedInstancesTests extends JsoniterTestBase {
  import JsoniterNestedInstancesTestDomain._

  object Imported extends KebsJsoniter
  object MixedIn  extends KebsJsoniter with PageCodecs
  object Flat     extends KebsJsoniter with CaseClass1ToValueClass

  test("derived types inside containers") {
    import Imported._
    roundTrip(HasList(List(item), 1), """{"x":[{"name":"i","n":1}],"n":1}""")
    roundTrip(HasOption(Some(item), 1), """{"x":{"name":"i","n":1},"n":1}""")
    roundTrip(HasMap(Map("k" -> item), 1), """{"x":{"k":{"name":"i","n":1}},"n":1}""")
    roundTrip(HasTuple((1, item), 1), """{"x":[1,{"name":"i","n":1}],"n":1}""")
    roundTrip(HasGen(Gen(item, 2), 1), """{"x":{"x":{"name":"i","n":1},"n":2},"n":1}""")
    roundTrip(
      Diamond0(Diamond1(item, item), Diamond1(item, item)),
      """{"a":{"a":{"name":"i","n":1},"b":{"name":"i","n":1}},"b":{"a":{"name":"i","n":1},"b":{"name":"i","n":1}}}"""
    )
  }

  test("a codec in the companion, as a field and at the root") {
    import Imported._
    roundTrip(Located("n", Point(1, 2), List(Point(3, 4))), """{"name":"n","at":"1,2","others":["3,4"]}""")
    roundTrip(Point(3, 4), "\"3,4\"")
    roundTrip(List(Point(3, 4)), "[\"3,4\"]")
  }

  test("a user's generic codecs of derived and recursive types, imported") {
    import Imported._, PageCodecs._
    roundTrip(HasPage(Page(List(item)), 1), """{"x":{"DATA":[{"name":"i","n":1}]},"n":1}""")
    roundTrip(HasPages(Page(List(Page(List(item)))), 1), """{"x":{"DATA":[{"DATA":[{"name":"i","n":1}]}]},"n":1}""")
    roundTrip(Page(List(item)), """{"DATA":[{"name":"i","n":1}]}""")
    roundTrip(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    roundTrip(Page(List(recPage)), """{"DATA":[{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}]}""")
    roundTrip(mutA, """{"name":"a","b":{"as":{"DATA":[{"name":"b","b":null}]},"n":2}}""")
    roundTrip(HasOpaque(Opaque(1, 2), 1), """{"x":{"OPAQUE":{"k":1,"i":{"name":"h","n":2}}},"n":1}""")
  }

  test("a user's generic codecs, mixed into the protocol") {
    import MixedIn._
    roundTrip(HasPage(Page(List(item)), 1), """{"x":{"DATA":[{"name":"i","n":1}]},"n":1}""")
    roundTrip(recPage, """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}""")
    roundTrip(List(mutA), """[{"name":"a","b":{"as":{"DATA":[{"name":"b","b":null}]},"n":2}}]""")
  }

  test("a user's generic codecs in the companion, requested through containers") {
    import Imported._
    val rec = RecBox(1, List(Box(List(RecBox(2, Nil)))))
    roundTrip(rec, """{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}""")
    roundTrip(List(Box(List(rec))), """[{"DATA":[{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}]}]""")
    val option = Box(List(RecOptionBox(1, Some(Box(List(RecOptionBox(2, None)))))))
    roundTrip(List(Option(option)), """[{"DATA":[{"v":1,"o":{"DATA":[{"v":2,"o":null}]}}]}]""")
    roundTrip(List((1, Box(List(RecTupleBox(1, List((2, Box(Nil)))))))), """[[1,{"DATA":[{"v":1,"o":[[2,{"DATA":[]}]]}]}]]""")
  }

  test("a user's generic codecs that need codecs of standard types") {
    import Imported._
    roundTrip(
      HasBoxes(Box(List(1)), Box(List("a")), Box(List(Box(List(true)))), Listed(List(item)), Listed(List(2L)), Maybe(Some(item), 1)),
      """{"ints":{"DATA":[1]},"strings":{"DATA":["a"]},"nested":{"DATA":[{"DATA":[true]}]},"listed":{"LISTED":[{"name":"i","n":1}]},""" +
        """"numbers":{"LISTED":[2]},"maybe":{"MAYBE":[{"name":"i","n":1},1]}}"""
    )
    roundTrip(Listed(List(item)), """{"LISTED":[{"name":"i","n":1}]}""")
    roundTrip(RecListed(1, Listed(List(RecListed(2, Listed(Nil))))), """{"v":1,"kids":{"LISTED":[{"v":2,"kids":{"LISTED":[]}}]}}""")
    roundTrip(Listed(List(RecListed(2, Listed(Nil)))), """{"LISTED":[{"v":2,"kids":{"LISTED":[]}}]}""")
  }

  test("a derived type hidden behind a user's codec") {
    import Imported._
    roundTrip(Knot(1, 2), """{"KNOT":{"DATA":[{"v":1,"kids":{"DATA":[]}}]}}""")
    roundTrip(HasKnot(Knot(1, 2), 1), """{"x":{"KNOT":{"DATA":[{"v":1,"kids":{"DATA":[]}}]}},"n":1}""")
  }

  test("recursion through containers") {
    import Imported._
    roundTrip(RecOption(1, Some(RecOption(2, None))), """{"v":1,"next":{"v":2,"next":null}}""")
    roundTrip(RecList(1, List(RecList(2, Nil))), """{"v":1,"kids":[{"v":2,"kids":[]}]}""")
    roundTrip(RecMap(1, Map("k" -> RecMap(2, Map.empty))), """{"v":1,"kids":{"k":{"v":2,"kids":{}}}}""")
    roundTrip(RecTuple(1, List("k" -> RecTuple(2, Nil))), """{"v":1,"kids":[["k",{"v":2,"kids":[]}]]}""")
    roundTrip(
      Map("k" -> List(RecMapOfLists(1, Map("j" -> List(RecMapOfLists(2, Map.empty)))))),
      """{"k":[{"v":1,"kids":{"j":[{"v":2,"kids":{}}]}}]}"""
    )
  }

  test("a local implicit wins: for a nested type, a standard type, a container") {
    import Imported._, PageCodecs._

    {
      implicit val itemCodec: JsonValueCodec[Item] = asString(s => Item(s, 0), i => s"${i.name}:${i.n}")
      writeToString(HasPage(Page(List(item)), 1)) shouldBe """{"x":{"DATA":["i:1"]},"n":1}"""
      writeToString(HasList(List(item), 1)) shouldBe """{"x":["i:1"],"n":1}"""
      writeToString(List(item)) shouldBe """["i:1"]"""
    }

    {
      implicit val intCodec: JsonValueCodec[Int] = asString(_.toInt, i => s"#$i")
      writeToString(HasList(List(item), 1)) shouldBe """{"x":[{"name":"i","n":"#1"}],"n":"#1"}"""
      writeToString(List(1)) shouldBe """["#1"]"""
      writeToString(Page(List(1))) shouldBe """{"DATA":["#1"]}"""
    }

    {
      implicit val itemsCodec: JsonValueCodec[List[Item]] = asString(_ => Nil, _.map(_.name).mkString("+"))
      writeToString(HasList(List(item, item), 1)) shouldBe """{"x":"i+i","n":1}"""
    }
  }

  test("nested types are derived with the protocol's config, read once per derivation") {
    var reads = 0
    object Upper extends KebsJsoniter with PageCodecs {
      override def kebsJsonConfig: JsonConfig = { reads += 1; super.kebsJsonConfig.withFieldNaming(NamingStyle.uppercase) }
    }
    import Upper._
    val codec = implicitly[JsonValueCodec[MutA]]
    roundTrip(mutA, """{"NAME":"a","B":{"AS":{"DATA":[{"NAME":"b","B":null}]},"N":2}}""")(codec)
    reads shouldBe 1
  }

  test("a type argument that a codec does not need is not derived") {
    import Imported._
    roundTrip(HasPhantom(Phantom("a"), List(Phantom("b"))), """{"id":"a","ids":["b"]}""")
  }

  test("types that grow are reported") {
    import Imported._
    "implicitly[JsonValueCodec[Grow[Int]]]" shouldNot typeCheck
  }

  test("the same JSON whichever way it is requested") {
    import Imported._, PageCodecs._
    val json = """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}"""
    roundTrip(recPage, json)
    roundTrip(recPage, json)(deriveCodec[RecPage])
    roundTrip(List(recPage), s"[$json]")
    roundTrip(List(recPage), s"[$json]")(deriveCodec[List[RecPage]])
    roundTrip(Map("k" -> recPage), s"""{"k":$json}""")
    roundTrip(Page(List(recPage)), s"""{"DATA":[$json]}""")
    roundTrip((1, recPage), s"[1,$json]")(deriveCodec[(Int, RecPage)])
    roundTrip(Option(recPage), json)(deriveCodec[Option[RecPage]])
    roundTrip(mutA.b.get, """{"as":{"DATA":[{"name":"b","b":null}]},"n":2}""")
  }

  test("the implicit being defined") {
    import Imported._
    roundTrip(RecOwn(1, List(Box(List(RecOwn(2, Nil))))), """{"v":1,"kids":[{"DATA":[{"v":2,"kids":[]}]}]}""")

    implicit lazy val codec: JsonValueCodec[RecList] = deriveCodec[RecList]
    roundTrip(List(RecList(1, List(RecList(2, Nil)))), """[{"v":1,"kids":[{"v":2,"kids":[]}]}]""")
  }

  test("a user's generic codecs of standard types, at the root") {
    import Imported._, PageCodecs._
    roundTrip(Box(List(1)), """{"DATA":[1]}""")
    roundTrip(Box(List("a")), """{"DATA":["a"]}""")
    roundTrip(Box(List((1, item))), """{"DATA":[[1,{"name":"i","n":1}]]}""")
    roundTrip(Box(List(Option(item), None)), """{"DATA":[{"name":"i","n":1},null]}""")
    roundTrip(Box(List(Box(List(1)))), """{"DATA":[{"DATA":[1]}]}""")
    roundTrip(Box(List(List(1))), """{"DATA":[[1]]}""")
    roundTrip(Box(List(item)), """{"DATA":[{"name":"i","n":1}]}""")
    roundTrip(Page(List(1)), """{"DATA":[1]}""")
    roundTrip(Page(List(Page(List("a")))), """{"DATA":[{"DATA":["a"]}]}""")
    roundTrip(Listed(List(1)), """{"LISTED":[1]}""")
    roundTrip(Maybe(Option(1), 2), """{"MAYBE":[1,2]}""")
    roundTrip(List(Box(List(1))), """[{"DATA":[1]}]""")
  }
}
