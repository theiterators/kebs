package pl.iterators.kebs.sprayjson.internal

import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import spray.json._

import scala.collection.Factory
import scala.collection.mutable

// called by code generated in SprayFormatMacrosImpl
object SprayDerivationRuntime {
  final class Fields(val names: Array[String], val writeNull: Boolean, val useDefaults: Boolean)

  // JsObject(members*) keeps them sorted, as spray's own formats (and kebs 2) printed them

  def product[T](config: JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      writeFields: (Fields, T) => List[JsField],
      readFields: (Fields, JsValue) => T
  ): RootJsonFormat[T] = new RootJsonFormat[T] {
    private lazy val fields = new Fields(
      scalaNames.zip(renamed).map { case (name, explicit) => explicit.getOrElse(config.fieldNaming(name)) }.toArray,
      config.noneHandling == NoneHandling.WriteNull,
      config.useDefaults
    )
    def write(obj: T): JsValue = JsObject(writeFields(fields, obj)*)
    def read(json: JsValue): T = readFields(fields, json)
  }

  // spray's jsonFormat0
  def singleton[T](value: T): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(obj: T): JsValue = JsObject.empty
    def read(json: JsValue): T = json match {
      case JsObject(_) => value
      case _           => throw DeserializationException("Object expected")
    }
  }

  def sum[T](config: JsonConfig, caseNames: List[String])(
      writeCase: (T, (Int, JsValue) => JsValue) => JsValue,
      readCase: (Int, JsValue) => T
  ): RootJsonFormat[T] = new RootJsonFormat[T] {
    private lazy val names    = caseNames.map(config.constructorNaming(_)).toArray
    private lazy val byName   = names.zipWithIndex.reverse.toMap
    private val discriminator = config.discriminator

    private val wrap: (Int, JsValue) => JsValue = discriminator match {
      case Some(field) =>
        (index, json) =>
          json match {
            case JsObject(members) => JsObject(members + (field -> JsString(names(index))))
            case _ => serializationError(s"${names(index)} is not written as a JSON object, so it cannot carry the '$field' discriminator")
          }
      case None => (index, json) => JsObject(names(index) -> json)
    }

    def write(obj: T): JsValue = writeCase(obj, wrap)

    def read(json: JsValue): T = (discriminator, json) match {
      case (Some(field), JsObject(members)) =>
        members.get(field) match {
          case Some(JsString(name)) => byName.get(name).fold(unknown(name))(readCase(_, json))
          case _                    => deserializationError(s"Object is missing required member '$field'", fieldNames = field :: Nil)
        }
      case (None, JsObject(members)) =>
        members
          .collectFirst { case (name, value) if byName.contains(name) => readCase(byName(name), value) }
          .getOrElse(unknown(members.keys.mkString(", ")))
      case _ => deserializationError("Object expected")
    }

    private def unknown(found: String): Nothing =
      deserializationError(s"Unknown case '$found', expected one of: ${names.mkString(", ")}")
  }

  def unreachableCase[T](unexpected: Any): T = throw new IllegalStateException(s"Not a case of the sealed hierarchy: $unexpected")

  def entry[A](fields: Fields, index: Int, value: A, writer: JsonWriter[A], rest: List[JsField]): List[JsField] =
    (fields.names(index), writer.write(value)) :: rest

  def optionEntry[A](fields: Fields, index: Int, value: Option[A], writer: JsonWriter[A], rest: List[JsField]): List[JsField] =
    value match {
      case Some(a)                  => (fields.names(index), writer.write(a)) :: rest
      case None if fields.writeNull => (fields.names(index), JsNull) :: rest
      case None                     => rest
    }

  // spray's ProductFormats.fromField, error messages and fieldNames included
  def read[A](json: JsValue, fields: Fields, index: Int, reader: JsonReader[A]): A = {
    val name = fields.names(index)
    json match {
      case x: JsObject =>
        try reader.read(x.fields(name))
        catch {
          case e: NoSuchElementException =>
            deserializationError("Object is missing required member '" + name + "'", e, name :: Nil)
          case DeserializationException(msg, cause, fieldNames) => deserializationError(msg, cause, name :: fieldNames)
        }
      case _ => deserializationError("Object expected in field '" + name + "'", fieldNames = name :: Nil)
    }
  }

  def readOr[A](json: JsValue, fields: Fields, index: Int, reader: JsonReader[A], default: => A): A =
    if (fields.useDefaults && isMissing(json, fields.names(index))) default else read(json, fields, index, reader)

  def readOption[A](json: JsValue, fields: Fields, index: Int, reader: JsonReader[A]): Option[A] =
    if (isMissing(json, fields.names(index))) None else read(json, fields, index, optionReader(reader))

  def readOptionOr[A](json: JsValue, fields: Fields, index: Int, reader: JsonReader[A], default: => Option[A]): Option[A] =
    if (isMissing(json, fields.names(index))) { if (fields.useDefaults) default else None }
    else read(json, fields, index, optionReader(reader))

  private def isMissing(json: JsValue, name: String): Boolean = json match {
    case x: JsObject => !x.fields.contains(name)
    case _           => false
  }

  private def optionReader[A](reader: JsonReader[A]): JsonReader[Option[A]] = {
    case JsNull => None
    case other  => Some(reader.read(other))
  }

  def asRoot[T](format: JsonFormat[T]): RootJsonFormat[T] = format match {
    case root: RootJsonFormat[T @unchecked] => root
    case _                                  =>
      new RootJsonFormat[T] {
        def write(obj: T): JsValue = format.write(obj)
        def read(json: JsValue): T = format.read(json)
      }
  }

  def optionFormat[A](inner: JsonFormat[A]): JsonFormat[Option[A]] = new JsonFormat[Option[A]] {
    def write(obj: Option[A]): JsValue = obj.fold[JsValue](JsNull)(inner.write)
    def read(json: JsValue): Option[A] = optionReader(inner).read(json)
  }

  def collectionFormat[A, R, C](inner: JsonFormat[A], iterate: C => Iterable[A], factory: Factory[A, R])(
      build: mutable.Builder[A, R] => C
  ): JsonFormat[C] = new JsonFormat[C] {
    def write(obj: C): JsValue = JsArray(iterate(obj).iterator.map(inner.write).toVector)
    def read(json: JsValue): C = json match {
      case JsArray(elements) =>
        val builder = factory.newBuilder
        elements.foreach(element => builder += inner.read(element))
        build(builder)
      case other => deserializationError("Expected Collection as JsArray, but got " + other)
    }
  }

  def stringMapFormat[V, P, R, C](inner: JsonFormat[V], iterate: C => Iterable[P], factory: Factory[P, R])(
      key: P => String,
      value: P => V,
      pair: (String, V) => P,
      build: mutable.Builder[P, R] => C
  ): JsonFormat[C] = new JsonFormat[C] {
    def write(obj: C): JsValue = JsObject(iterate(obj).iterator.map(p => key(p) -> inner.write(value(p))).toMap)
    def read(json: JsValue): C = json match {
      case JsObject(members) =>
        val builder = factory.newBuilder
        members.foreach { case (k, v) => builder += pair(k, inner.read(v)) }
        build(builder)
      case other => deserializationError("Expected Map as JsObject, but got " + other)
    }
  }

  def flatFormat[T, A](rep: ValueClassLike[T, A], inner: JsonFormat[A]): JsonFormat[T] = new JsonFormat[T] {
    def write(obj: T): JsValue = inner.write(rep.unapply(obj))
    def read(json: JsValue): T = rep.apply(inner.read(json))
  }
}
