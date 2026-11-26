package pl.iterators.kebs.playjson.internal

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.playjson.KebsPlayJsonMacros
import play.api.libs.json.{OWrites, Reads, Writes}

import scala.language.experimental.macros

// called by the code the entry points generate
object PlayLocalInstances {
  def reads[T](config: JsonConfig): Reads[T] = macro KebsPlayJsonMacros.oneReads[T]
  def writes[T](config: JsonConfig): OWrites[T] = macro KebsPlayJsonMacros.oneWrites[T]
  def collectionWrites[T](config: JsonConfig): Writes[T] = macro KebsPlayJsonMacros.oneCollectionWrites[T]
}
