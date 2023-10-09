package domain

object Color extends Enumeration {
  type Color = Value
  val Red, Green, Blue = Value
}
object ColorDomain {
  val colorValues = Color.values.toList
  type colorType = Color.Color
}