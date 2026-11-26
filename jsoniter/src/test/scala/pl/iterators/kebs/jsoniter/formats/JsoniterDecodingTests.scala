package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.jsoniter.KebsJsoniter

object JsoniterDecodingTestDomain {
  final case class Two(a: Int, b: List[String])
  final case class Colliding(Aa: Int, BB: Int, AaBB: Int, BBAa: Int)
  final case class Box(two: Two)
  final case class Outer(box: Option[Box], n: Int)
}

class JsoniterDecodingTests extends JsoniterTestBase {
  import JsoniterDecodingTestDomain._

  object Protocol extends KebsJsoniter
  import Protocol.deriveCodec

  implicit private val two: JsonValueCodec[Two]             = deriveCodec[Two]
  implicit private val colliding: JsonValueCodec[Colliding] = deriveCodec[Colliding]
  implicit private val outer: JsonValueCodec[Outer]         = deriveCodec[Outer]

  test("keys in any order, unknown ones skipped") {
    readFromString[Two]("""{"b":["x"],"a":1}""") shouldBe Two(1, List("x"))
    readFromString[Two]("""{"z":{"a":[1,{"b":null}]},"a":1,"y":"}","b":[],"x":[[]]}""") shouldBe Two(1, Nil)
    readFromString[Two](""" { "a" : 1 , "b" : [ "x" , "y" ] } """) shouldBe Two(1, List("x", "y"))
    readFromString[Two]("""{"a":1,"b":[]}""") shouldBe Two(1, Nil)
  }

  test("keys with the same hash") {
    "Aa".hashCode shouldBe "BB".hashCode
    readFromString[Colliding]("""{"BBAa":4,"AaBB":3,"BB":2,"Aa":1}""") shouldBe Colliding(1, 2, 3, 4)
  }

  test("what is not a value of the type") {
    rejected[Two]("[1]", "")
    rejected[Two]("null", "")
    rejected[Two]("\"x\"", "")
    rejected[Two]("", "")
    rejected[Two]("""{"a":null,"b":[]}""", "")
    rejected[Two]("""{"a":"1","b":[]}""", "")
    rejected[Two]("""{"a":1,"b":null}""", "")
    rejected[Two]("""{"a":1,"b":[null]}""", "")
    rejected[Two]("""{"a":1,"b":{}}""", "")
    rejected[Two]("""{"a":1.5,"b":[]}""", "")
  }

  test("missing and duplicate keys are named") {
    rejected[Two]("""{"b":[]}""", "\"a\"")
    rejected[Two]("""{"a":1}""", "\"b\"")
    rejected[Two]("{}", "\"a\"")
    rejected[Two]("""{"a":1,"b":[],"a":1}""", "\"a\"")
  }

  test("malformed input") {
    val json = """{"a":1,"b":["x"]}"""
    (0 until json.length).foreach(length => rejected[Two](json.take(length), ""))
    rejected[Two]("""{"a":1,"b":[],}""", "")
    rejected[Two]("""{"a":1,"b":["x",]}""", "")
    rejected[Two]("""{"a":1 "b":[]}""", "")
  }

  test("an Option's value is not skipped") {
    readFromString[Outer]("""{"n":1,"box":{"two":{"a":1,"b":[]}}}""") shouldBe Outer(Some(Box(Two(1, Nil))), 1)
    rejected[Outer]("""{"n":1,"box":{}}""", "\"two\"")
    rejected[Outer]("""{"n":1,"box":nul}""", "")
  }
}
