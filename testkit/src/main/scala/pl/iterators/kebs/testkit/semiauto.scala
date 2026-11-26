package pl.iterators.kebs.testkit

import pl.iterators.kebs.core.macros.ValueClass

object semiauto {
  final case class Id(value: String)
  object Id { implicit val valueClass: ValueClass[Id] = ValueClass.derived }

  final case class Inner(fooBar: Int)
  final case class Outer(inner: Inner, list: List[Inner], id: Id)

  sealed trait Shape
  final case class Circle(radius: Int) extends Shape
  case object Empty                    extends Shape

  val outer: Outer       = Outer(Inner(1), List(Inner(2)), Id("x"))
  val outerJson: String  = """{"inner":{"foo_bar":1},"list":[{"foo_bar":2}],"id":"x"}"""
  val circleJson: String = """{"Circle":{"radius":3}}"""
}
