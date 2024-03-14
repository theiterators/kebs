package pl.iterators.kebs.enums.domain

object Color extends Enumeration {
  type Color = Value
  val Red, Green, Blue = Value
}
object ColorDomain {
  val colorValues = Color.values.toList
  type ColorType = Color.Color
}