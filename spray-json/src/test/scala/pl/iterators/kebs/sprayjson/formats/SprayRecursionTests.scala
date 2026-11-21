package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.sprayjson.KebsSprayJson
import spray.json._

object SprayRecursionTestDomain {
  final case class Thing(id: String, parent: Option[Thing])
  final case class Tree(value: Int, children: List[Tree])
  final case class Node(edges: Map[String, Node])
  final case class A(name: String, b: Option[B])
  final case class B(as: List[A], n: Int)
  final case class InnerPair(a: Int, b: String)
  final case class Wrapper(inner: InnerPair)
  final case class HasWrapper(w: Wrapper, n: Int)
}

class SprayRecursionTests extends AnyFunSuite with Matchers {
  import SprayRecursionTestDomain._

  object Protocol extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass
  import Protocol._

  private def roundTrip[T](value: T, expected: String)(implicit format: RootJsonFormat[T]) = {
    format.write(value) shouldBe expected.parseJson
    format.read(expected.parseJson) shouldBe value
  }

  test("recursion through Option") {
    roundTrip(Thing("c", Some(Thing("p", None))), """{"id":"c","parent":{"id":"p"}}""")
  }

  test("recursion through List") {
    roundTrip(Tree(1, List(Tree(2, Nil))), """{"value":1,"children":[{"value":2,"children":[]}]}""")
  }

  test("recursion through Map") {
    roundTrip(Node(Map("x" -> Node(Map.empty))), """{"edges":{"x":{"edges":{}}}}""")
  }

  test("mutual recursion") {
    roundTrip(A("a", Some(B(List(A("b", None)), 1))), """{"name":"a","b":{"as":[{"name":"b"}],"n":1}}""")
  }

  test("a single-field case class wrapping a derived one is flat") {
    roundTrip(HasWrapper(Wrapper(InnerPair(1, "x")), 2), """{"w":{"a":1,"b":"x"},"n":2}""")
  }
}
