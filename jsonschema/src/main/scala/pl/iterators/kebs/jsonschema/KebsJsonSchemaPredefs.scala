package pl.iterators.kebs.jsonschema

import json.schema.Predef

import java.net.{URI, URL}
import java.time.{YearMonth, ZonedDateTime}

trait KebsJsonSchemaPredefs {
  implicit val predefZonedDateTime: Predef[ZonedDateTime] = Predef(json.Schema.`string`[ZonedDateTime])

  implicit val predefYearMonth: Predef[YearMonth] = Predef(json.Schema.`string`[YearMonth])

  implicit val predefUrl: Predef[URL] = Predef(json.Schema.`string`[URL])

  implicit val predefUri: Predef[URI] = Predef(json.Schema.`string`[URI])
}
