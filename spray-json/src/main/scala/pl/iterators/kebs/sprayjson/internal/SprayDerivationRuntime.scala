package pl.iterators.kebs.sprayjson.internal

import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.internal.LocalCodecs
import spray.json._

// called by code generated in SprayFormatMacrosImpl
object SprayDerivationRuntime {
  final class Fields(val names: Array[String], val writeNull: Boolean, val useDefaults: Boolean)

  final class Members(fields: Fields) {
    private val members = List.newBuilder[JsField]

    def result(): List[JsField] = members.result()

    // the static type may be a type parameter that stands for an Option: spray's own formats look at the OptionFormat too
    def add[A](index: Int, value: A, writer: JsonWriter[A]): Unit =
      if (value != None || fields.writeNull || !isOptionFormat(writer)) members += ((fields.names(index), writer.write(value)))

    def option[A](index: Int, value: Option[A], writer: JsonWriter[A]): Unit = value match {
      case Some(a)                  => members += ((fields.names(index), writer.write(a)))
      case None if fields.writeNull => members += ((fields.names(index), JsNull))
      case None                     => ()
    }
  }

  private def isOptionFormat(format: Any): Boolean = format.isInstanceOf[StandardFormats#OptionFormat[?]]

  // JsObject(members*) keeps them sorted, as spray's own formats (and kebs 2) printed them

  def product[T](config: => JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      writeFields: (Members, T) => Unit,
      readFields: (Fields, JsValue) => T
  ): RootJsonFormat[T] = new RootJsonFormat[T] {
    private lazy val fields = {
      val current = config
      new Fields(
        scalaNames.zip(renamed).map { case (name, explicit) => explicit.getOrElse(current.fieldNaming(name)) }.toArray,
        current.noneHandling == NoneHandling.WriteNull,
        current.useDefaults
      )
    }
    def write(obj: T): JsValue = {
      val members = new Members(fields)
      writeFields(members, obj)
      JsObject(members.result()*)
    }
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

  def sum[T](config: => JsonConfig, caseNames: List[String], tests: List[Any => Boolean], codecs: LocalCodecs): RootJsonFormat[T] =
    new RootJsonFormat[T] {
      private lazy val names         = caseNames.map(config.constructorNaming(_)).toArray
      private lazy val byName        = names.zipWithIndex.reverse.toMap
      private lazy val discriminator = config.discriminator

      private lazy val wrap: (Int, JsValue) => JsValue = discriminator match {
        case Some(field) =>
          (index, json) =>
            json match {
              case JsObject(members) if members.get(field).exists(_ != JsString(names(index))) =>
                serializationError(s"${names(index)} has a field '$field' of its own, so it cannot carry the '$field' discriminator")
              case JsObject(members) => JsObject(members + (field -> JsString(names(index))))
              case _                 =>
                serializationError(s"${names(index)} is not written as a JSON object, so it cannot carry the '$field' discriminator")
            }
        case None => (index, json) => JsObject(names(index) -> json)
      }

      def write(obj: T): JsValue = {
        val index = tests.indexWhere(_(obj))
        if (index < 0) throw new IllegalStateException(s"Not a case of the sealed hierarchy: $obj")
        wrap(index, codecs.get[JsonFormat[T]](index).write(obj))
      }

      private def readCase(index: Int, json: JsValue): T = codecs.get[JsonFormat[T]](index).read(json)

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

  // spray's ProductFormats.fromField, error messages and fieldNames included
  def read[A](json: JsValue, fields: Fields, index: Int, reader: JsonReader[A]): A = {
    val name = fields.names(index)
    json match {
      case x: JsObject if !x.fields.contains(name) && isOptionFormat(reader) => None.asInstanceOf[A]
      case x: JsObject                                                       =>
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
}
