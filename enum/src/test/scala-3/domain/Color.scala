package domain

enum Color {
  case Red, Green, Blue
}
object ColorDomain {
  val colorValues = Color.values.toList
  type colorType = Color
}