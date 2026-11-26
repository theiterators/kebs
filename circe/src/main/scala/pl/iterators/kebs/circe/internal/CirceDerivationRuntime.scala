package pl.iterators.kebs.circe.internal

import cats.data.{NonEmptyList, Validated}
import io.circe.DecodingFailure.Reason.{CustomReason, WrongTypeExpectation}
import io.circe._
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.LocalCodecs

import scala.collection.mutable

// called by code generated in CirceMacrosImpl; what is decoded, in which order and with which errors follows circe-generic
object CirceDerivationRuntime {
  final class Members(names: Array[String], omitNone: Boolean) {
    private val members = List.newBuilder[(String, Json)]

    def result(): List[(String, Json)] = members.result()

    def add[A](index: Int, value: A, encoder: Encoder[A]): Unit = members += ((names(index), encoder(value)))

    def option[A](index: Int, value: Option[A], encoder: Encoder[A]): Unit = value match {
      case Some(a)          => members += ((names(index), encoder(a)))
      case None if omitNone => ()
      case None             => members += ((names(index), Json.Null))
    }
  }

  private def fieldNames(config: JsonConfig, scalaNames: List[String], renamed: List[Option[String]]): Array[String] =
    scalaNames.zip(renamed).map { case (name, explicit) => explicit.getOrElse(config.fieldNaming(name)) }.toArray

  def productEncoder[T](config: => JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      writeFields: (Members, T) => Unit
  ): Encoder.AsObject[T] = new Encoder.AsObject[T] {
    private lazy val names    = fieldNames(config, scalaNames, renamed)
    private lazy val omitNone = config.noneHandling == NoneHandling.Omit

    def encodeObject(obj: T): JsonObject = {
      val members = new Members(names, omitNone)
      writeFields(members, obj)
      JsonObject.fromIterable(members.result())
    }
  }

  sealed trait FieldDecoder {
    def decode(json: HCursor, name: String, useDefaults: Boolean): Decoder.Result[Any]
    def decodeAccumulating(json: HCursor, name: String, useDefaults: Boolean): Decoder.AccumulatingResult[Any]
  }

  def field[A](decoder: Decoder[A]): FieldDecoder = new PresentField(decoder, None)

  def fieldOr[A](decoder: Decoder[A], default: => A): FieldDecoder = new PresentField(decoder, Some(() => default))

  def transientField[A](default: => A): FieldDecoder = new FieldDecoder {
    def decode(json: HCursor, name: String, useDefaults: Boolean): Decoder.Result[Any]                         = Right(default)
    def decodeAccumulating(json: HCursor, name: String, useDefaults: Boolean): Decoder.AccumulatingResult[Any] = Validated.valid(default)
  }

  final private class PresentField[A](decoder: Decoder[A], default: Option[() => A]) extends FieldDecoder {
    private def orDefault[R](json: HCursor, name: String, useDefaults: Boolean)(withDefault: A => R)(decode: ACursor => R): R = {
      val cursor = json.downField(name)
      default match {
        case Some(value) if useDefaults && json.value.isObject && cursor.failed => withDefault(value())
        case _                                                                  => decode(cursor)
      }
    }

    def decode(json: HCursor, name: String, useDefaults: Boolean): Decoder.Result[Any] =
      orDefault[Decoder.Result[Any]](json, name, useDefaults)(Right(_))(decoder.tryDecode)

    def decodeAccumulating(json: HCursor, name: String, useDefaults: Boolean): Decoder.AccumulatingResult[Any] =
      orDefault[Decoder.AccumulatingResult[Any]](json, name, useDefaults)(Validated.valid)(decoder.tryDecodeAccumulating)
  }

  private def notAnObject(json: HCursor): DecodingFailure = DecodingFailure(WrongTypeExpectation("object", json.value), json.history)

  // the field decoders are by name: they refer to other local instances, which may refer back to this one
  def productDecoder[T](config: => JsonConfig, scalaNames: List[String], renamed: List[Option[String]])(
      fieldDecoders: => List[FieldDecoder],
      construct: Array[Any] => T
  ): Decoder[T] = new Decoder[T] {
    private lazy val names       = fieldNames(config, scalaNames, renamed)
    private lazy val decoders    = fieldDecoders.toArray
    private lazy val useDefaults = config.useDefaults

    private def constructed(json: HCursor, values: Array[Any]): Decoder.Result[T] =
      ValueClassLike.catching(construct)(values).left.map(DecodingFailure(_, json.history))

    def apply(json: HCursor): Decoder.Result[T] =
      if (decoders.isEmpty && !json.value.isObject) Left(notAnObject(json))
      else {
        val values                           = new Array[Any](decoders.length)
        var failure: Option[DecodingFailure] = None
        var index                            = 0
        while (index < decoders.length && failure.isEmpty) {
          decoders(index).decode(json, names(index), useDefaults) match {
            case Right(value) => values(index) = value
            case Left(error)  => failure = Some(error)
          }
          index += 1
        }
        failure.toLeft(values).flatMap(constructed(json, _))
      }

    override def decodeAccumulating(json: HCursor): Decoder.AccumulatingResult[T] =
      if (decoders.isEmpty && !json.value.isObject) Validated.invalidNel(notAnObject(json))
      else {
        val values   = new Array[Any](decoders.length)
        val failures = mutable.ListBuffer.empty[DecodingFailure]
        decoders.indices.foreach { index =>
          decoders(index).decodeAccumulating(json, names(index), useDefaults) match {
            case Validated.Valid(value)    => values(index) = value
            case Validated.Invalid(errors) => failures ++= errors.toList
          }
        }
        NonEmptyList.fromList(failures.toList).fold(Validated.fromEither(constructed(json, values)).toValidatedNel)(Validated.invalid(_))
      }
  }

  def value[A](values: Array[Any], index: Int): A = values(index).asInstanceOf[A]

  def singletonEncoder[T]: Encoder.AsObject[T] = Encoder.AsObject.instance[T](_ => JsonObject.empty)

  def singletonDecoder[T](value: T): Decoder[T] =
    Decoder.instance(json => if (json.value.isObject) Right(value) else Left(notAnObject(json)))

  def sumEncoder[T](
      config: => JsonConfig,
      caseNames: List[String],
      tests: List[Any => Boolean],
      codecs: LocalCodecs
  ): Encoder.AsObject[T] = new Encoder.AsObject[T] {
    private lazy val names = caseNames.map(config.constructorNaming(_)).toArray

    private lazy val wrap: (Int, Json) => JsonObject = config.discriminator match {
      case Some(field) =>
        (index, json) =>
          json.asObject match {
            case Some(obj) if obj(field).exists(_ != Json.fromString(names(index))) =>
              throw new IllegalArgumentException(
                s"${names(index)} has a field '$field' of its own, so it cannot carry the '$field' discriminator"
              )
            case Some(obj) => obj.add(field, Json.fromString(names(index)))
            case None      =>
              throw new IllegalArgumentException(
                s"${names(index)} is not written as a JSON object, so it cannot carry the '$field' discriminator"
              )
          }
      case None => (index, json) => JsonObject.singleton(names(index), json)
    }

    def encodeObject(obj: T): JsonObject = {
      val index = tests.indexWhere(_(obj))
      if (index < 0) throw new IllegalStateException(s"Not a case of the sealed hierarchy: $obj")
      wrap(index, codecs.get[Encoder[T]](index)(obj))
    }
  }

  def sumDecoder[T](config: => JsonConfig, caseNames: List[String], codecs: LocalCodecs): Decoder[T] = new Decoder[T] {
    private lazy val names = caseNames.map(config.constructorNaming(_))

    private def decoderOf(name: String): Option[Decoder[T]] = names.indexOf(name) match {
      case -1    => None
      case index => Some(codecs.get[Decoder[T]](index))
    }

    private def unknown(found: String, json: HCursor): DecodingFailure =
      DecodingFailure(CustomReason(s"Unknown case '$found', expected one of: ${names.mkString(", ")}"), json.history)

    private lazy val discriminator = config.discriminator

    private def select(json: HCursor): Decoder.Result[(Decoder[T], ACursor)] = discriminator match {
      case Some(field) =>
        json.downField(field).as[String].flatMap(name => decoderOf(name).map(_ -> json).toRight(unknown(name, json)))
      case None =>
        json.keys match {
          case None       => Left(notAnObject(json))
          case Some(keys) =>
            names
              .find(name => json.downField(name).succeeded)
              .flatMap(name => decoderOf(name).map(_ -> json.downField(name)))
              .toRight(unknown(keys.mkString(", "), json))
        }
    }

    def apply(json: HCursor): Decoder.Result[T] = select(json).flatMap { case (decoder, cursor) => decoder.tryDecode(cursor) }

    override def decodeAccumulating(json: HCursor): Decoder.AccumulatingResult[T] =
      select(json).fold(Validated.invalidNel, { case (decoder, cursor) => decoder.tryDecodeAccumulating(cursor) })
  }

  def asObject[T](encoder: Encoder[T]): Encoder.AsObject[T] = encoder.asInstanceOf[Encoder.AsObject[T]]
}
