package pl.iterators.kebs.jsonschema

import json.Schema

@deprecated("kebs-jsonschema is deprecated: scala-jsonschema is no longer maintained", "3.0.0")
final case class JsonSchemaWrapper[T](schema: Schema[T]) extends AnyVal
