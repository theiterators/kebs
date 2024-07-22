package pl.iterators.kebs.enums.domain

enum Color {
  case Red, Green, Blue
}
object ColorDomain {
  val colorValues = Color.values.toList
  type ColorType = Color
}
