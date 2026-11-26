package pl.iterators.kebs.playjson.internal

import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.LocalCodecs
import play.api.libs.json._

// called by code generated in PlayJsonMacrosImpl; field reading is play's own JsPath API, so errors are play's
object PlayDerivationRuntime {
  final class Fields(val names: Array[String], val writeNull: Boolean, val useDefaults: Boolean)

  final class Members(fields: Fields) {
    private val members = List.newBuilder[(String, JsValue)]

    def result(): List[(String, JsValue)] = members.result()

    def add[A](index: Int, value: A, writes: Writes[A]): Unit = members += ((fields.names(index), writes.writes(value)))

    def option[A](index: Int, value: Option[A], writes: Writes[A]): Unit = value match {
      case Some(a)                  => members += ((fields.names(index), writes.writes(a)))
      case None if fields.writeNull => members += ((fields.names(index), JsNull))
      case None                     => ()
    }
  }

  private def fields(config: JsonConfig, scalaNames: List[String], renamed: List[Option[String]]): Fields =
    new Fields(
      scalaNames.zip(renamed).map { case (name, explicit) => explicit.getOrElse(config.fieldNaming(name)) }.toArray,
      config.noneHandling == NoneHandling.WriteNull,
      config.useDefaults
    )

  def productReads[T](config: => JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      readFields: (Fields, JsValue) => List[JsResult[Any]],
      construct: Array[Any] => T
  ): Reads[T] = new Reads[T] {
    private lazy val names = fields(config, scalaNames, renamed)
    // as Json.reads: field paths of a non-object would all be "missing", and all-Option products would read garbage
    def reads(json: JsValue): JsResult[T] = json match {
      case obj: JsObject => combine(readFields(names, obj), construct)
      case _             => JsError("error.expected.jsobject")
    }
  }

  def productWrites[T](config: => JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      writeFields: (Members, T) => Unit
  ): OWrites[T] = new OWrites[T] {
    private lazy val names       = fields(config, scalaNames, renamed)
    def writes(obj: T): JsObject = {
      val members = new Members(names)
      writeFields(members, obj)
      JsObject(members.result())
    }
  }

  def field[A](json: JsValue, fields: Fields, index: Int, reads: Reads[A]): JsResult[A] = {
    implicit val fieldReads: Reads[A] = reads
    (JsPath \ fields.names(index)).read[A].reads(json)
  }

  def fieldOr[A](json: JsValue, fields: Fields, index: Int, reads: Reads[A], default: => A): JsResult[A] =
    if (fields.useDefaults) {
      implicit val fieldReads: Reads[A] = reads
      (JsPath \ fields.names(index)).readWithDefault[A](default).reads(json)
    } else field(json, fields, index, reads)

  def optionField[A](json: JsValue, fields: Fields, index: Int, reads: Reads[A]): JsResult[Option[A]] = {
    implicit val fieldReads: Reads[A] = reads
    (JsPath \ fields.names(index)).readNullable[A].reads(json)
  }

  def optionFieldOr[A](json: JsValue, fields: Fields, index: Int, reads: Reads[A], default: => Option[A]): JsResult[Option[A]] =
    if (fields.useDefaults) {
      implicit val fieldReads: Reads[A] = reads
      (JsPath \ fields.names(index)).readNullableWithDefault[A](default).reads(json)
    } else optionField(json, fields, index, reads)

  // every field's errors are reported, as Json.reads does
  private def combine[T](results: List[JsResult[Any]], build: Array[Any] => T): JsResult[T] =
    results.collect { case error: JsError => error } match {
      case Nil => ValueClassLike.catching(build)(results.map(_.asInstanceOf[JsSuccess[Any]].value).toArray).fold(JsError(_), JsSuccess(_))
      case errors => errors.reduce((left: JsError, right: JsError) => JsError.merge(left, right))
    }

  def value[A](values: Array[Any], index: Int): A = values(index).asInstanceOf[A]

  def singletonReads[T](value: T): Reads[T] = {
    case JsObject(_) => JsSuccess(value)
    case _           => JsError("error.expected.jsobject")
  }

  def singletonWrites[T]: OWrites[T] = OWrites[T](_ => JsObject.empty)

  def sumReads[T](config: => JsonConfig, caseNames: List[String], codecs: LocalCodecs): Reads[T] = new Reads[T] {
    private lazy val names  = caseNames.map(config.constructorNaming(_)).toArray
    private lazy val byName = names.zipWithIndex.reverse.toMap

    private lazy val discriminator = config.discriminator

    private def readCase(index: Int, json: JsValue): JsResult[T] = codecs.get[Reads[T]](index).reads(json)

    private def unknown(found: String): JsError =
      JsError(s"Unknown case '$found', expected one of: ${names.mkString(", ")}")

    def reads(json: JsValue): JsResult[T] = (discriminator, json) match {
      case (Some(field), obj: JsObject) =>
        obj.value.get(field) match {
          case Some(JsString(name)) => byName.get(name).fold[JsResult[T]](unknown(name))(readCase(_, json))
          case _                    => JsError(JsPath \ field, "error.path.missing")
        }
      case (None, obj: JsObject) =>
        obj.value
          .collectFirst { case (name, value) if byName.contains(name) => underCase(name, readCase(byName(name), value)) }
          .getOrElse(unknown(obj.keys.mkString(", ")))
      case _ => JsError("error.expected.jsobject")
    }
  }

  // errors point into the wrapper member; a success keeps the root's empty path
  private def underCase[T](name: String, result: JsResult[T]): JsResult[T] = result match {
    case error: JsError => error.repath(JsPath \ name)
    case success        => success
  }

  def sumWrites[T](config: => JsonConfig, caseNames: List[String], tests: List[Any => Boolean], codecs: LocalCodecs): OWrites[T] =
    new OWrites[T] {
      private lazy val names = caseNames.map(config.constructorNaming(_)).toArray

      private lazy val wrap: (Int, JsValue) => JsObject = config.discriminator match {
        case Some(field) =>
          (index, json) =>
            json match {
              case obj: JsObject if obj.value.get(field).exists(_ != JsString(names(index))) =>
                throw new IllegalArgumentException(
                  s"${names(index)} has a field '$field' of its own, so it cannot carry the '$field' discriminator"
                )
              case obj: JsObject => obj + (field -> JsString(names(index)))
              case _             =>
                throw new IllegalArgumentException(
                  s"${names(index)} is not written as a JSON object, so it cannot carry the '$field' discriminator"
                )
            }
        case None => (index, json) => JsObject(Seq(names(index) -> json))
      }

      def writes(obj: T): JsObject = {
        val index = tests.indexWhere(_(obj))
        if (index < 0) throw new IllegalStateException(s"Not a case of the sealed hierarchy: $obj")
        wrap(index, codecs.get[Writes[T]](index).writes(obj))
      }
    }

  def collectionWrites[F, A](element: Writes[A]): Writes[F] =
    Writes[F](collection => JsArray(collection.asInstanceOf[Iterable[A]].iterator.map(element.writes).toVector))

  def asOWrites[T](writes: Writes[T]): OWrites[T] = writes match {
    case owrites: OWrites[T @unchecked] => owrites
    case _                              =>
      OWrites[T] { obj =>
        writes.writes(obj) match {
          case json: JsObject => json
          case other          => throw new IllegalArgumentException(s"Expected a JSON object, got $other")
        }
      }
  }
}
