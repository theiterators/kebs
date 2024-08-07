package pl.iterators.kebs

import spray.json.DefaultJsonProtocol

package object sprayjson extends KebsSprayJson with DefaultJsonProtocol {
  object snakified   extends KebsSprayJsonSnakified with DefaultJsonProtocol
  object capitalized extends KebsSprayJsonCapitalized with DefaultJsonProtocol
}
