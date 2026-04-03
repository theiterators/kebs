package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.jsoniter.KebsJsoniter

package object jsoniter extends KebsJsoniter {
  object snakified   extends KebsJsoniterSnakified
  object capitalized extends KebsJsoniterCapitalized
}
