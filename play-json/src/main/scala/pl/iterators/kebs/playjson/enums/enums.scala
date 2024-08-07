package pl.iterators.kebs.playjson

package object enums extends KebsPlayJsonEnums with KebsPlayJsonValueEnums {
  object uppercase extends KebsCirceEnumsUppercase with KebsPlayJsonValueEnums
  object lowercase extends KebsCirceEnumsLowercase with KebsPlayJsonValueEnums
}
