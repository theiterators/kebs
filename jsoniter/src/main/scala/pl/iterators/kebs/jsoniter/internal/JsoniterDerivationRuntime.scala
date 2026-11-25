package pl.iterators.kebs.jsoniter.internal

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.macros.ValueClassLike
import pl.iterators.kebs.core.macros.internal.LocalCodecs

import scala.collection.Factory
import scala.collection.mutable.ArrayBuilder
import scala.reflect.ClassTag
import scala.util.Try
import scala.util.control.NonFatal

// called by code generated in JsoniterMacrosImpl. Codecs of parts are looked up when they are used: they may refer back.
object JsoniterDerivationRuntime {

  // ignores the default it is given
  abstract class OwnCodec[A] extends JsonValueCodec[A] {
    // the value of a field that is missing, if it may be
    def whenMissing: Option[A] = None
  }

  abstract class ObjectCodec[T] extends OwnCodec[T] {
    def encodeFields(x: T, out: JsonWriter): Unit
    def isField(name: String): Boolean

    final def encodeValue(x: T, out: JsonWriter): Unit = {
      out.writeObjectStart()
      encodeFields(x, out)
      out.writeObjectEnd()
    }
    final def nullValue: T = null.asInstanceOf[T]
  }

  private object Missing

  sealed abstract class PartCodec {
    def get: JsonValueCodec[Any]
  }

  def local(codecs: LocalCodecs, index: Int): PartCodec = new PartCodec {
    def get: JsonValueCodec[Any] = codecs.get[JsonValueCodec[Any]](index)
  }

  def standard[A](codec: JsonValueCodec[A]): PartCodec = new PartCodec {
    val get: JsonValueCodec[Any] = codec.asInstanceOf[JsonValueCodec[Any]]
  }

  sealed abstract class Field {
    def isTransient: Boolean = false
    def read(in: JsonReader): Any
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any
  }

  def required(codec: PartCodec): Field = new Field {
    def read(in: JsonReader): Any                                        = readWith(codec.get, in)
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = whenMissing(codec, in, name)
  }

  def requiredOr(codec: PartCodec, default: => Any): Field = new Field {
    def read(in: JsonReader): Any                                        = readWith(codec.get, in)
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = if (useDefaults) default else whenMissing(codec, in, name)
  }

  private def whenMissing(codec: PartCodec, in: JsonReader, name: String): Any = codec.get match {
    case own: OwnCodec[Any] => own.whenMissing.getOrElse(in.requiredFieldError(name))
    case _                  => in.requiredFieldError(name)
  }

  def optional(codec: PartCodec): Field = optionalOr(codec, None)

  def optionalOr(codec: PartCodec, default: => Any): Field = new Field {
    def read(in: JsonReader): Any                                        = readOption(codec.get, in)
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = if (useDefaults) default else None
  }

  def ignored(default: => Any): Field = new Field {
    override def isTransient: Boolean                                    = true
    def read(in: JsonReader): Any                                        = default
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = default
  }

  // of fields or cases; excluded: which are not read
  final class Keys(val names: Array[String], excluded: Int => Boolean) {
    private val isPlain: Array[Boolean] = names.map(_.forall(JsonWriter.isNonEscapedAscii))
    private val isRead: Array[Boolean]  = names.indices.map(!excluded(_)).toArray
    private val mask: Int               = Integer.highestOneBit(names.length * 4 + 1) * 2 - 1
    private val table: Array[Int]       = {
      val slots = Array.fill(mask + 1)(-1)
      names.indices.filter(isRead(_)).foreach { index =>
        val chars = names(index).toCharArray
        var slot  = JsonReader.toHashCode(chars, chars.length) & mask
        while (slots(slot) >= 0) slot = (slot + 1) & mask
        slots(slot) = index
      }
      slots
    }

    // of the key or string that was just read; expected: the one to try first
    def indexOf(in: JsonReader, length: Int, expected: Int): Int =
      if (expected < names.length && isRead(expected) && in.isCharBufEqualsTo(length, names(expected))) expected
      else {
        var slot = in.charBufToHashCode(length) & mask
        while (table(slot) >= 0 && !in.isCharBufEqualsTo(length, names(table(slot)))) slot = (slot + 1) & mask
        table(slot)
      }

    def contains(name: String): Boolean = names.indices.exists(index => names(index) == name && isRead(index))

    def write(out: JsonWriter, index: Int): Unit =
      if (isPlain(index)) out.writeNonEscapedAsciiKey(names(index)) else out.writeKey(names(index))
  }

  final class Fields(names: Array[String], val fields: Array[Field], val writeNull: Boolean, val useDefaults: Boolean) {
    val keys: Keys = new Keys(names, fields(_).isTransient)

    def writeKey(out: JsonWriter, index: Int): Unit = keys.write(out, index)
  }

  def writeInt(fields: Fields, out: JsonWriter, index: Int, value: Int): Unit = {
    fields.writeKey(out, index)
    out.writeVal(value)
  }

  def writeLong(fields: Fields, out: JsonWriter, index: Int, value: Long): Unit = {
    fields.writeKey(out, index)
    out.writeVal(value)
  }

  def writeDouble(fields: Fields, out: JsonWriter, index: Int, value: Double): Unit = {
    fields.writeKey(out, index)
    out.writeVal(value)
  }

  def writeBoolean(fields: Fields, out: JsonWriter, index: Int, value: Boolean): Unit = {
    fields.writeKey(out, index)
    out.writeVal(value)
  }

  def writeString(fields: Fields, out: JsonWriter, index: Int, value: String): Unit = {
    fields.writeKey(out, index)
    out.writeVal(value)
  }

  // an Option may have an instance that is kebs' codec of Options
  def write[A](fields: Fields, out: JsonWriter, index: Int, value: A, codec: JsonValueCodec[A]): Unit =
    if (fields.writeNull || (value.asInstanceOf[AnyRef] ne None) || !codec.isInstanceOf[OptionCodec[?]]) {
      fields.writeKey(out, index)
      codec.encodeValue(value, out)
    }

  def writeOption[A](fields: Fields, out: JsonWriter, index: Int, value: Option[A], codec: JsonValueCodec[A]): Unit = value match {
    case Some(a) =>
      fields.writeKey(out, index)
      codec.encodeValue(a, out)
    case None if fields.writeNull =>
      fields.writeKey(out, index)
      out.writeNull()
    case None => ()
  }

  def value[A](values: Array[Any], index: Int): A = values(index).asInstanceOf[A]

  def product[T](config: => JsonConfig, scalaNames: List[String], renamed: List[Option[String]], fieldList: List[Field])(
      writeFields: (Fields, JsonWriter, T) => Unit,
      construct: Array[Any] => T
  ): JsonValueCodec[T] = new ObjectCodec[T] {
    private lazy val fields: Fields = {
      val current = config
      new Fields(
        scalaNames.zip(renamed).map { case (name, explicit) => explicit.getOrElse(current.fieldNaming(name)) }.toArray,
        fieldList.toArray,
        writeNull = current.noneHandling == NoneHandling.WriteNull,
        useDefaults = current.useDefaults
      )
    }

    def isField(name: String): Boolean            = fields.keys.contains(name)
    def encodeFields(x: T, out: JsonWriter): Unit = writeFields(fields, out, x)

    def decodeValue(in: JsonReader, default: T): T = {
      val current = fields
      val keys    = current.keys
      val size    = keys.names.length
      val values  = new Array[Any](size)
      var index   = 0
      while (index < size) {
        values(index) = Missing
        index += 1
      }
      if (!in.isNextToken('{')) in.decodeError("expected '{'")
      if (!in.isNextToken('}')) {
        in.rollbackToken()
        var expected = 0
        var more     = true
        while (more) {
          val length = in.readKeyAsCharBuf()
          index = keys.indexOf(in, length, expected)
          if (index < 0) in.skip()
          else if (isMissing(values(index))) {
            values(index) = current.fields(index).read(in)
            expected = index + 1
          } else in.duplicatedKeyError(length)
          more = in.isNextToken(',')
        }
        if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
      }
      index = 0
      while (index < size) {
        if (isMissing(values(index))) values(index) = current.fields(index).missing(in, keys.names(index), current.useDefaults)
        index += 1
      }
      construct(values)
    }

    private def isMissing(value: Any): Boolean = value.asInstanceOf[AnyRef] eq Missing
  }

  def singleton[T](value: T): JsonValueCodec[T] = new ObjectCodec[T] {
    def isField(name: String): Boolean             = false
    def encodeFields(x: T, out: JsonWriter): Unit  = ()
    def decodeValue(in: JsonReader, default: T): T = {
      readObject(in) {
        in.readKeyAsCharBuf()
        in.skip()
      }
      value
    }
  }

  final private class Cases(val discriminator: Option[String], names: Array[String]) {
    val keys: Keys = new Keys(names, _ => false)

    def unknown(in: JsonReader, name: String): Nothing =
      in.decodeError(s"unknown case '$name', expected one of: ${names.mkString(", ")}")
  }

  def sum[T](config: => JsonConfig, caseNames: List[String], tests: List[Any => Boolean], codecs: LocalCodecs): JsonValueCodec[T] =
    new OwnCodec[T] {
      private val isCase: Array[Any => Boolean] = tests.toArray
      private lazy val cases: Cases             = {
        val current = config
        new Cases(current.discriminator, caseNames.map(current.constructorNaming(_)).toArray)
      }

      private def codec(index: Int): JsonValueCodec[T] = codecs.get[JsonValueCodec[T]](index)

      def encodeValue(x: T, out: JsonWriter): Unit = {
        val current = cases
        var index   = 0
        while (index < isCase.length && !isCase(index)(x)) index += 1
        if (index == isCase.length) throw new IllegalStateException(s"Not a case of the sealed hierarchy: $x")
        current.discriminator match {
          case None =>
            out.writeObjectStart()
            current.keys.write(out, index)
            codec(index).encodeValue(x, out)
            out.writeObjectEnd()
          case Some(field) => encodeDiscriminated(x, out, codec(index), field, current.keys.names(index))
        }
      }

      def decodeValue(in: JsonReader, default: T): T = {
        val current = cases
        current.discriminator match {
          case None        => readWrapped(in, current)
          case Some(field) => readDiscriminated(in, current, field)
        }
      }

      // marked, so that the name of an unknown case can be read again for the error
      private def readWrapped(in: JsonReader, current: Cases): T = {
        if (!in.isNextToken('{')) in.decodeError(s"expected an object with one of: ${current.keys.names.mkString(", ")}")
        if (in.isNextToken('}')) current.unknown(in, "")
        in.rollbackToken()
        in.setMark()
        val index = current.keys.indexOf(in, in.readKeyAsCharBuf(), 0)
        if (index < 0) {
          in.rollbackToMark()
          current.unknown(in, in.readKeyAsString())
        }
        in.resetMark()
        val value = readWith(codec(index), in)
        if (!in.isNextToken('}')) in.decodeError("expected '}': one case per object")
        value
      }

      private def readDiscriminated(in: JsonReader, current: Cases, field: String): T = {
        in.setMark()
        if (!in.isNextToken('{')) in.decodeError("expected '{'")
        if (in.isNextToken('}')) in.requiredFieldError(field)
        in.rollbackToken()
        if (!in.skipToKey(field)) in.requiredFieldError(field)
        val name = in.readString(null)
        if (name eq null) in.decodeError("unexpected null")
        val index = current.keys.names.indexOf(name)
        if (index < 0) current.unknown(in, name)
        in.rollbackToMark()
        readWith(codec(index), in)
      }

      def nullValue: T = null.asInstanceOf[T]
    }

  private def encodeDiscriminated[T](x: T, out: JsonWriter, codec: JsonValueCodec[T], field: String, name: String): Unit = {
    def reject(reason: String): Nothing = out.encodeError(s"$name $reason, so it cannot carry the '$field' discriminator")
    codec match {
      case codec: ObjectCodec[T] =>
        if (codec.isField(field)) reject(s"has a field '$field' of its own")
        out.writeObjectStart()
        out.writeKey(field)
        out.writeVal(name)
        codec.encodeFields(x, out)
        out.writeObjectEnd()
      case _ =>
        // not kebs' codec: its object is written again, with the discriminator
        val written = writeToArrayReentrant(x)(codec)
        if (written.isEmpty || written(0) != '{') reject("is not written as a JSON object")
        if (readFromArrayReentrant(written)(hasField(field))) reject(s"has a field '$field' of its own")
        val start = writeToArrayReentrant(Map(field -> name))(discriminatorObject).dropRight(1)
        val rest  = written.drop(1)
        out.writeRawVal(if (rest.length == 1) start ++ rest else (start :+ ','.toByte) ++ rest)
    }
  }

  private def hasField(field: String): JsonValueCodec[Boolean] = new JsonValueCodec[Boolean] {
    def decodeValue(in: JsonReader, default: Boolean): Boolean = {
      var found = false
      readObject(in) {
        found |= in.readKeyAsString() == field
        in.skip()
      }
      found
    }
    def encodeValue(x: Boolean, out: JsonWriter): Unit = out.writeVal(x)
    def nullValue: Boolean                             = false
  }

  private val discriminatorObject: JsonValueCodec[Map[String, String]] =
    map[String, String, Map[String, String]](LocalCodecs(List(() => JsoniterCodecs.string)), JsoniterCodecs.string, Map)

  def valueClass[T, A](codecs: LocalCodecs, rep: ValueClassLike[T, A]): JsonValueCodec[T]      = wrapped(codecs, rep.apply, rep.unapply)
  def converted[T, A](codecs: LocalCodecs, rep: InstanceConverter[T, A]): JsonValueCodec[T]    = wrapped(codecs, rep.decode, rep.encode)
  def valueClassKey[T, A](base: JsonKeyCodec[A], rep: ValueClassLike[T, A]): JsonKeyCodec[T]   = wrappedKey(base, rep.apply, rep.unapply)
  def convertedKey[T, A](base: JsonKeyCodec[A], rep: InstanceConverter[T, A]): JsonKeyCodec[T] = wrappedKey(base, rep.decode, rep.encode)

  private def wrapped[T, A](codecs: LocalCodecs, wrap: A => T, unwrap: T => A): JsonValueCodec[T] = new OwnCodec[T] {
    private def codec: JsonValueCodec[A] = codecs.get[JsonValueCodec[A]](0)

    def decodeValue(in: JsonReader, default: T): T = wrapping(in, readWith(codec, in), wrap)
    def encodeValue(x: T, out: JsonWriter): Unit   = codec.encodeValue(unwrap(x), out)

    override def whenMissing: Option[T] = codec match {
      case own: OwnCodec[A] => own.whenMissing.flatMap(a => Try(wrap(a)).toOption)
      case _                => None
    }

    // for other codecs, which may keep it unboxed: null is not a value of an AnyVal class, and the empty value may be rejected
    lazy val nullValue: T = {
      val empty = codec.nullValue
      if (empty == null) null.asInstanceOf[T] else Try(wrap(empty)).getOrElse(null.asInstanceOf[T])
    }
  }

  private def wrappedKey[T, A](base: JsonKeyCodec[A], wrap: A => T, unwrap: T => A): JsonKeyCodec[T] = new JsonKeyCodec[T] {
    def decodeKey(in: JsonReader): T           = wrapping(in, base.decodeKey(in), wrap)
    def encodeKey(x: T, out: JsonWriter): Unit = base.encodeKey(unwrap(x), out)
  }

  private def wrapping[T, A](in: JsonReader, value: A, wrap: A => T): T =
    try wrap(value)
    catch { case NonFatal(error) => in.decodeError(String.valueOf(error.getMessage)) }

  final class OptionCodec[A](codecs: LocalCodecs) extends OwnCodec[Option[A]] {
    def decodeValue(in: JsonReader, default: Option[A]): Option[A] = readOption(codecs.get[JsonValueCodec[A]](0), in)
    def encodeValue(x: Option[A], out: JsonWriter): Unit           = x match {
      case Some(a) => codecs.get[JsonValueCodec[A]](0).encodeValue(a, out)
      case None    => out.writeNull()
    }
    def nullValue: Option[A]                    = None
    override def whenMissing: Option[Option[A]] = Some(None)
  }

  def option[A](codecs: LocalCodecs): JsonValueCodec[Option[A]] = new OptionCodec[A](codecs)

  def iterable[A, C](codecs: LocalCodecs, factory: Factory[A, C]): JsonValueCodec[C] = new OwnCodec[C] {
    def decodeValue(in: JsonReader, default: C): C = {
      val codec   = codecs.get[JsonValueCodec[A]](0)
      val builder = factory.newBuilder
      readArray(in)(builder += readWith(codec, in))
      builder.result()
    }
    def encodeValue(x: C, out: JsonWriter): Unit = {
      val codec = codecs.get[JsonValueCodec[A]](0)
      out.writeArrayStart()
      x.asInstanceOf[Iterable[A]].foreach(codec.encodeValue(_, out))
      out.writeArrayEnd()
    }
    // what codecs that are not kebs' read a missing one as
    def nullValue: C = factory.newBuilder.result()
  }

  def array[A](codecs: LocalCodecs, tag: ClassTag[A]): JsonValueCodec[Array[A]] = new OwnCodec[Array[A]] {
    implicit private val elementTag: ClassTag[A] = tag

    def decodeValue(in: JsonReader, default: Array[A]): Array[A] = {
      val codec   = codecs.get[JsonValueCodec[A]](0)
      val builder = ArrayBuilder.make[A]
      readArray(in)(builder += readWith(codec, in))
      builder.result()
    }
    def encodeValue(x: Array[A], out: JsonWriter): Unit = {
      val codec = codecs.get[JsonValueCodec[A]](0)
      out.writeArrayStart()
      x.foreach(codec.encodeValue(_, out))
      out.writeArrayEnd()
    }
    def nullValue: Array[A] = Array.empty[A]
  }

  def map[K, V, M](codecs: LocalCodecs, keys: JsonKeyCodec[K], factory: Factory[(K, V), M]): JsonValueCodec[M] = new OwnCodec[M] {
    def decodeValue(in: JsonReader, default: M): M = {
      val codec   = codecs.get[JsonValueCodec[V]](0)
      val builder = factory.newBuilder
      readObject(in) {
        val key = keys.decodeKey(in)
        builder += ((key, readWith(codec, in)))
      }
      builder.result()
    }
    def encodeValue(x: M, out: JsonWriter): Unit = {
      val codec = codecs.get[JsonValueCodec[V]](0)
      out.writeObjectStart()
      x.asInstanceOf[Iterable[(K, V)]].foreach { case (key, value) =>
        keys.encodeKey(key, out)
        codec.encodeValue(value, out)
      }
      out.writeObjectEnd()
    }
    def nullValue: M = factory.newBuilder.result()
  }

  // indices: of the elements' codecs
  def tuple[T](codecs: LocalCodecs, indices: List[Int])(construct: Array[Any] => T): JsonValueCodec[T] = new OwnCodec[T] {
    private val size                               = indices.size
    def decodeValue(in: JsonReader, default: T): T = {
      val values = new Array[Any](size)
      var index  = 0
      readArray(in) {
        if (index >= size) in.decodeError(s"expected an array of $size elements")
        values(index) = readWith(codecs.get[JsonValueCodec[Any]](indices(index)), in)
        index += 1
      }
      if (index < size) in.decodeError(s"expected an array of $size elements")
      construct(values)
    }
    def encodeValue(x: T, out: JsonWriter): Unit = {
      out.writeArrayStart()
      x.asInstanceOf[Product].productIterator.zip(indices.iterator).foreach { case (element, index) =>
        codecs.get[JsonValueCodec[Any]](index).encodeValue(element, out)
      }
      out.writeArrayEnd()
    }
    def nullValue: T = null.asInstanceOf[T]
  }

  private def readWith[A](codec: JsonValueCodec[A], in: JsonReader): A =
    codec.decodeValue(in, if (codec.isInstanceOf[OwnCodec[?]]) null.asInstanceOf[A] else codec.nullValue)

  private def readOption[A](codec: JsonValueCodec[A], in: JsonReader): Option[A] =
    if (in.isNextToken('n')) in.readNullOrError(None, "expected value or null")
    else {
      in.rollbackToken()
      Some(readWith(codec, in))
    }

  private def readObject(in: JsonReader)(readEntry: => Unit): Unit = {
    if (!in.isNextToken('{')) in.decodeError("expected '{'")
    if (!in.isNextToken('}')) {
      in.rollbackToken()
      readEntry
      while (in.isNextToken(',')) readEntry
      if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
    }
  }

  private def readArray(in: JsonReader)(readElement: => Unit): Unit = {
    if (!in.isNextToken('[')) in.decodeError("expected '['")
    if (!in.isNextToken(']')) {
      in.rollbackToken()
      readElement
      while (in.isNextToken(',')) readElement
      if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
    }
  }
}
