package pl.iterators.kebs.jsoniter.golden

import com.github.plokhotnyuk.jsoniter_scala.core.*
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.formats.JsoniterTestBase

object JsoniterScala3AdtTestDomain {
  enum Color { case Red, DarkBlue }
  enum Shape {
    case Circle(r: Double)
    case Rect(a: Int, b: Int)
    case Nothing
  }
  enum Tree[+A] {
    case Node(left: Tree[A], value: A, right: Tree[A])
    case Leaf
  }
}

class JsoniterScala3AdtTests extends JsoniterTestBase {
  import JsoniterScala3AdtTestDomain.*

  object Protocol extends KebsJsoniter
  import Protocol.*

  test("enums without enum codecs") {
    roundTrip[Color](Color.DarkBlue, """{"DarkBlue":{}}""")
    roundTrip[Shape](Shape.Circle(1.0), """{"Circle":{"r":1.0}}""")
    roundTrip[Shape](Shape.Nothing, """{"Nothing":{}}""")
  }

  test("generic recursive enums") {
    roundTrip[Tree[Int]](
      Tree.Node(Tree.Leaf, 1, Tree.Node(Tree.Leaf, 2, Tree.Leaf)),
      """{"Node":{"left":{"Leaf":{}},"value":1,"right":{"Node":{"left":{"Leaf":{}},"value":2,"right":{"Leaf":{}}}}}}"""
    )
  }
}
