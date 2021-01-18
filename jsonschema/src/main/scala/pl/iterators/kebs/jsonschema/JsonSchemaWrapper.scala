package pl.iterators.kebs.jsonschema

import json.Schema

final case class JsonSchemaWrapper[T](schema: Schema[T]) extends AnyVal
