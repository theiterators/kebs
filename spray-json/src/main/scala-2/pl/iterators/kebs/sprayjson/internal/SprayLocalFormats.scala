package pl.iterators.kebs.sprayjson.internal

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.sprayjson.KebsSprayJsonMacros
import spray.json.RootJsonFormat

import scala.language.experimental.macros

// called by the code the entry points generate
object SprayLocalFormats {
  def level[T](config: JsonConfig): RootJsonFormat[T] = macro KebsSprayJsonMacros.level[T]
  def existing[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.existing[T]
}
