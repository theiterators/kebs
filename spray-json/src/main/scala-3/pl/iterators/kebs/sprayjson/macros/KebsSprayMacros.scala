package pl.iterators.kebs.sprayjson.macros

import scala.deriving._
import scala.compiletime._
import spray.json.*
import pl.iterators.kebs.sprayjson.FieldNamingStrategy

// this is largely inspired by https://github.com/paoloboni/spray-json-derived-codecs
object KebsSprayMacros {
  inline private def label[A]: String = constValue[A].asInstanceOf[String]

  inline def summonAllFormats[A <: Tuple]: List[JsonFormat[_]] = {
    inline erasedValue[A] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[JsonFormat[t]] :: summonAllFormats[ts]
  }

  inline def summonAllLabels[A <: Tuple]: List[String] = {
    inline erasedValue[A] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        label[t] :: summonAllLabels[ts]
    }
  }

  inline def writeElems[T](formats: List[JsonFormat[_]], namingStrategy: FieldNamingStrategy, nullOptions: Boolean)(obj: T): JsValue = {
    val pElem = obj.asInstanceOf[Product]
    (pElem.productElementNames.toList
      .zip(pElem.productIterator.toList)
      .zip(formats))
      .map { case ((label, elem), format) =>
        elem match {
          case None if !nullOptions =>
            JsObject.empty
          case e =>
            JsObject(namingStrategy.transform(label) -> format.asInstanceOf[JsonFormat[Any]].write(e))
        }
      }
      .foldLeft(JsObject.empty) { case (obj, encoded) =>
        JsObject(obj.fields ++ encoded.fields)
      }
  }

  inline def readElems[T](
      p: Mirror.ProductOf[T]
  )(labels: List[String], formats: List[JsonFormat[_]], namingStrategy: FieldNamingStrategy)(json: JsValue): T = {
    val decodedElems = (labels.map(namingStrategy.transform).zip(formats)).map { case (label, format) =>
      format.read(json.asJsObject.fields.getOrElse(label, JsNull))
    }
    p.fromProduct(Tuple.fromArray(decodedElems.toArray).asInstanceOf)
  }

  inline def materializeRootFormat[T <: Product](
      nullOptions: Boolean
  )(using m: Mirror.Of[T], namingStrategy: FieldNamingStrategy): RootJsonFormat[T] = {
    lazy val formats = summonAllFormats[m.MirroredElemTypes]
    lazy val labels  = summonAllLabels[m.MirroredElemLabels]

    val format = new RootJsonFormat[T] {
      override def read(json: JsValue): T = inline m match {
        case s: Mirror.SumOf[T]     => error("Sum types are not supported")
        case p: Mirror.ProductOf[T] => readElems(p)(labels, formats, namingStrategy)(json)
      }
      override def write(obj: T): JsValue = inline m match {
        case s: Mirror.SumOf[T]     => error("Sum types are not supported")
        case p: Mirror.ProductOf[T] => writeElems(formats, namingStrategy, nullOptions)(obj)
      }
    }
    format
  }
}
