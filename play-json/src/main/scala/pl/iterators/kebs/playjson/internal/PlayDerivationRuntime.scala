package pl.iterators.kebs.playjson.internal

import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import play.api.libs.json._

import scala.collection.Factory
import scala.collection.mutable
import scala.util.Try

// called by code generated in PlayJsonMacrosImpl; field reading is play's own JsPath API, so errors are play's
object PlayDerivationRuntime {
  final class Fields(val names: Array[String], val writeNull: Boolean, val useDefaults: Boolean)

  private def fields(config: JsonConfig, scalaNames: List[String], renamed: List[Option[String]]): Fields =
    new Fields(
      scalaNames.zip(renamed).map { case (name, explicit) => explicit.getOrElse(config.fieldNaming(name)) }.toArray,
      config.noneHandling == NoneHandling.WriteNull,
      config.useDefaults
    )

  def productReads[T](config: JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      readFields: (Fields, JsValue) => JsResult[T]
  ): Reads[T] = new Reads[T] {
    private lazy val names                = fields(config, scalaNames, renamed)
    def reads(json: JsValue): JsResult[T] = readFields(names, json)
  }

  def productWrites[T](config: JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      writeFields: (Fields, T) => List[(String, JsValue)]
  ): OWrites[T] = new OWrites[T] {
    private lazy val names       = fields(config, scalaNames, renamed)
    def writes(obj: T): JsObject = JsObject(writeFields(names, obj))
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

  def prepend(head: JsResult[Any], rest: List[JsResult[Any]]): List[JsResult[Any]] = head :: rest

  // every field's errors are reported, as Json.reads does
  def combine[T](results: List[JsResult[Any]], build: Array[Any] => T): JsResult[T] =
    results.collect { case error: JsError => error } match {
      case Nil    => JsSuccess(build(results.map(_.asInstanceOf[JsSuccess[Any]].value).toArray))
      case errors => errors.reduce((left: JsError, right: JsError) => JsError.merge(left, right))
    }

  def value[A](values: Array[Any], index: Int): A = values(index).asInstanceOf[A]

  def entry[A](fields: Fields, index: Int, value: A, writes: Writes[A], rest: List[(String, JsValue)]): List[(String, JsValue)] =
    (fields.names(index), writes.writes(value)) :: rest

  def optionEntry[A](
      fields: Fields,
      index: Int,
      value: Option[A],
      writes: Writes[A],
      rest: List[(String, JsValue)]
  ): List[(String, JsValue)] =
    value match {
      case Some(a)                  => (fields.names(index), writes.writes(a)) :: rest
      case None if fields.writeNull => (fields.names(index), JsNull) :: rest
      case None                     => rest
    }

  def singletonReads[T](value: T): Reads[T] = {
    case JsObject(_) => JsSuccess(value)
    case _           => JsError("error.expected.jsobject")
  }

  def singletonWrites[T]: OWrites[T] = OWrites[T](_ => JsObject.empty)

  def sumReads[T](config: JsonConfig, caseNames: List[String])(readCase: (Int, JsValue) => JsResult[T]): Reads[T] = new Reads[T] {
    private lazy val names  = caseNames.map(config.constructorNaming(_)).toArray
    private lazy val byName = names.zipWithIndex.reverse.toMap

    private def unknown(found: String): JsError =
      JsError(s"Unknown case '$found', expected one of: ${names.mkString(", ")}")

    def reads(json: JsValue): JsResult[T] = (config.discriminator, json) match {
      case (Some(field), obj: JsObject) =>
        obj.value.get(field) match {
          case Some(JsString(name)) => byName.get(name).fold[JsResult[T]](unknown(name))(readCase(_, json))
          case _                    => JsError(JsPath \ field, "error.path.missing")
        }
      case (None, obj: JsObject) =>
        obj.value
          .collectFirst { case (name, value) if byName.contains(name) => readCase(byName(name), value).repath(JsPath \ name) }
          .getOrElse(unknown(obj.keys.mkString(", ")))
      case _ => JsError("error.expected.jsobject")
    }
  }

  def sumWrites[T](config: JsonConfig, caseNames: List[String])(writeCase: (T, (Int, JsValue) => JsObject) => JsObject): OWrites[T] =
    new OWrites[T] {
      private lazy val names = caseNames.map(config.constructorNaming(_)).toArray

      private val wrap: (Int, JsValue) => JsObject = config.discriminator match {
        case Some(field) =>
          (index, json) =>
            json match {
              case obj: JsObject => obj + (field -> JsString(names(index)))
              case _             =>
                throw new IllegalArgumentException(
                  s"${names(index)} is not written as a JSON object, so it cannot carry the '$field' discriminator"
                )
            }
        case None => (index, json) => JsObject(Seq(names(index) -> json))
      }

      def writes(obj: T): JsObject = writeCase(obj, wrap)
    }

  def unreachableCase[T](unexpected: Any): T = throw new IllegalStateException(s"Not a case of the sealed hierarchy: $unexpected")

  def optionReads[A](inner: Reads[A]): Reads[Option[A]] = {
    case JsNull => JsSuccess(None)
    case other  => inner.reads(other).map(Some(_))
  }

  def optionWrites[A](inner: Writes[A]): Writes[Option[A]] = Writes[Option[A]](_.fold[JsValue](JsNull)(inner.writes))

  def collectionReads[A, R, C](inner: Reads[A], factory: Factory[A, R])(build: mutable.Builder[A, R] => C): Reads[C] = {
    case JsArray(elements) =>
      val results = elements.iterator.zipWithIndex.map { case (element, index) => inner.reads(element).repath(JsPath(index)) }.toList
      results.collect { case error: JsError => error } match {
        case Nil =>
          val builder = factory.newBuilder
          results.foreach(result => builder += result.asInstanceOf[JsSuccess[A]].value)
          JsSuccess(build(builder))
        case errors => errors.reduce((left: JsError, right: JsError) => JsError.merge(left, right))
      }
    case _ => JsError("error.expected.jsarray")
  }

  def collectionWrites[A, C](inner: Writes[A], iterate: C => Iterable[A]): Writes[C] =
    Writes[C](collection => JsArray(iterate(collection).iterator.map(inner.writes).toVector))

  def stringMapReads[V, P, R, C](inner: Reads[V], factory: Factory[P, R])(
      pair: (String, V) => P,
      build: mutable.Builder[P, R] => C
  ): Reads[C] = {
    case obj: JsObject =>
      val results = obj.value.iterator.map { case (key, value) => inner.reads(value).repath(JsPath \ key).map(pair(key, _)) }.toList
      results.collect { case error: JsError => error } match {
        case Nil =>
          val builder = factory.newBuilder
          results.foreach(result => builder += result.asInstanceOf[JsSuccess[P]].value)
          JsSuccess(build(builder))
        case errors => errors.reduce((left: JsError, right: JsError) => JsError.merge(left, right))
      }
    case _ => JsError("error.expected.jsobject")
  }

  def stringMapWrites[V, P, C](inner: Writes[V], iterate: C => Iterable[P])(key: P => String, value: P => V): OWrites[C] =
    OWrites[C](collection => JsObject(iterate(collection).iterator.map(p => key(p) -> inner.writes(value(p))).toSeq))

  // as KebsPlayJson.flatReads: a throwing (validating) apply becomes a JsError
  def flatReads[T, A](rep: ValueClassLike[T, A], inner: Reads[A]): Reads[T] =
    inner.flatMapResult(value => Try(rep.apply(value)).fold(error => JsError(error.getMessage), JsSuccess(_)))

  def flatWrites[T, A](rep: ValueClassLike[T, A], inner: Writes[A]): Writes[T] = inner.contramap(rep.unapply)

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
