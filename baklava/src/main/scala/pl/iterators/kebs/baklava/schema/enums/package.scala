package pl.iterators.kebs.baklava.schema

package object enums extends KebsBaklavaEnumsSchema with KebsBaklavaValueEnumsSchema {
  object uppercase extends KebsBaklavaEnumsUppercaseSchema with KebsBaklavaValueEnumsSchema
  object lowercase extends KebsBaklavaEnumsLowercaseSchema with KebsBaklavaValueEnumsSchema
}
