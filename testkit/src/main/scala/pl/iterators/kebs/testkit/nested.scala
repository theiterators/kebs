package pl.iterators.kebs.testkit

// derived types inside containers, users' generic types and themselves
object nested {
  final case class Item(name: String, n: Int)
  final case class Wrapper(inner: Item)
  final case class Page[A](items: List[A])
  final case class Gen[A](x: A, n: Int)
  final case class Hidden(k: Int, i: Item)
  final case class Opaque(v: Int, w: Int)

  final case class HasList(x: List[Item], n: Int)
  final case class HasOption(x: Option[Item], n: Int)
  final case class HasMap(x: Map[String, Item], n: Int)
  final case class HasTuple(x: (Int, Item), n: Int)
  final case class HasEither(x: Either[String, Item], n: Int)
  final case class HasPage(x: Page[Item], n: Int)
  final case class HasPages(x: Page[Page[Item]], n: Int)
  final case class HasWrapper(x: Wrapper, n: Int)
  final case class HasGen(x: Gen[Item], n: Int)
  final case class HasOpaque(x: Opaque, n: Int)

  final case class RecOption(v: Int, next: Option[RecOption])
  final case class RecList(v: Int, kids: List[RecList])
  final case class RecMap(v: Int, kids: Map[String, RecMap])
  final case class RecMapOfLists(v: Int, kids: Map[String, List[RecMapOfLists]])
  final case class RecPage(v: Int, kids: Page[RecPage])
  final case class RecPages(v: Int, kids: List[Page[RecPages]])
  final case class RecOptionPage(v: Int, o: Option[Page[RecOptionPage]])
  final case class RecTuple(v: Int, kids: List[(String, RecTuple)])
  final case class RecTuplePage(v: Int, o: List[(Int, Page[RecTuplePage])])
  final case class RecEither(v: Int, next: Either[String, RecEither])
  final case class MutA(name: String, b: Option[MutB])
  final case class MutB(as: Page[MutA], n: Int)

  final case class FlatTree(value: Int, children: FlatChildren)
  final case class FlatChildren(list: List[FlatTree])

  sealed trait Adt
  final case class Leaf(n: Int, i: Item)              extends Adt
  final case class Branch(l: Page[Adt], r: List[Adt]) extends Adt
  case object Empty                                   extends Adt
  final case class HasAdt(x: Adt, n: Int)

  final case class Diamond0(a: Diamond1, b: Diamond1)
  final case class Diamond1(a: Item, b: Item)

  final case class Grow[A](a: A, next: Option[Grow[List[A]]])

  val item: Item       = Item("i", 1)
  val adt: Adt         = Branch(Page(List(Leaf(1, item), Empty)), List(Empty))
  val recPage: RecPage = RecPage(1, Page(List(RecPage(2, Page(Nil)))))
  val mutA: MutA       = MutA("a", Some(MutB(Page(List(MutA("b", None))), 2)))
}
