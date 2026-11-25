package pl.iterators.kebs.jsoniter.internal

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonValueCodec, JsonWriter}
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

  abstract class ObjectCodec[T] extends JsonValueCodec[T] {
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

  sealed abstract class Field {
    def isTransient: Boolean = false
    def read(in: JsonReader): Any
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any
  }

  def required(codecs: LocalCodecs, index: Int): Field = new Field {
    def read(in: JsonReader): Any                                        = readWith(codecs.get[JsonValueCodec[Any]](index), in)
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = in.requiredFieldError(name)
  }

  def requiredOr(codecs: LocalCodecs, index: Int, default: => Any): Field = new Field {
    def read(in: JsonReader): Any                                        = readWith(codecs.get[JsonValueCodec[Any]](index), in)
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = if (useDefaults) default else in.requiredFieldError(name)
  }

  def optional(codecs: LocalCodecs, index: Int): Field = optionalOr(codecs, index, None)

  def optionalOr(codecs: LocalCodecs, index: Int, default: => Any): Field = new Field {
    def read(in: JsonReader): Any                                        = readOption(codecs.get[JsonValueCodec[Any]](index), in)
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = if (useDefaults) default else None
  }

  def ignored(default: => Any): Field = new Field {
    override def isTransient: Boolean                                    = true
    def read(in: JsonReader): Any                                        = default
    def missing(in: JsonReader, name: String, useDefaults: Boolean): Any = default
  }

  final class Fields(val names: Array[String], val fields: Array[Field], val writeNull: Boolean, val useDefaults: Boolean) {
    private val isPlain: Array[Boolean] = names.map(_.forall(JsonWriter.isNonEscapedAscii))
    private val mask: Int               = Integer.highestOneBit(names.length * 4 + 1) * 2 - 1
    private val table: Array[Int]       = {
      val slots = Array.fill(mask + 1)(-1)
      names.indices.filterNot(fields(_).isTransient).foreach { index =>
        val chars = names(index).toCharArray
        var slot  = JsonReader.toHashCode(chars, chars.length) & mask
        while (slots(slot) >= 0) slot = (slot + 1) & mask
        slots(slot) = index
      }
      slots
    }

    def indexOf(in: JsonReader, length: Int, expected: Int): Int =
      if (expected < names.length && !fields(expected).isTransient && in.isCharBufEqualsTo(length, names(expected))) expected
      else {
        var slot = in.charBufToHashCode(length) & mask
        while (table(slot) >= 0 && !in.isCharBufEqualsTo(length, names(table(slot)))) slot = (slot + 1) & mask
        table(slot)
      }

    def isField(name: String): Boolean = names.indices.exists(index => names(index) == name && !fields(index).isTransient)

    def writeKey(out: JsonWriter, index: Int): Unit =
      if (isPlain(index)) out.writeNonEscapedAsciiKey(names(index)) else out.writeKey(names(index))
  }

  def write[A](fields: Fields, out: JsonWriter, index: Int, value: A, codec: JsonValueCodec[A]): Unit = {
    fields.writeKey(out, index)
    codec.encodeValue(value, out)
  }

  def writeOption[A](fields: Fields, out: JsonWriter, index: Int, value: Option[A], codec: JsonValueCodec[A]): Unit = value match {
    case Some(a)                  => write(fields, out, index, a, codec)
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

    def isField(name: String): Boolean            = fields.isField(name)
    def encodeFields(x: T, out: JsonWriter): Unit = writeFields(fields, out, x)

    def decodeValue(in: JsonReader, default: T): T = {
      val current  = fields
      val size     = current.names.length
      val values   = Array.fill[Any](size)(Missing)
      var expected = 0
      readObject(in) {
        val length = in.readKeyAsCharBuf()
        val index  = current.indexOf(in, length, expected)
        if (index < 0) in.skip()
        else if (values(index) != Missing) in.duplicatedKeyError(length)
        else {
          values(index) = current.fields(index).read(in)
          expected = index + 1
        }
      }
      var index = 0
      while (index < size) {
        if (values(index) == Missing) values(index) = current.fields(index).missing(in, current.names(index), current.useDefaults)
        index += 1
      }
      construct(values)
    }
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

  def valueClass[T, A](codecs: LocalCodecs, rep: ValueClassLike[T, A]): JsonValueCodec[T]      = wrapped(codecs, rep.apply, rep.unapply)
  def converted[T, A](codecs: LocalCodecs, rep: InstanceConverter[T, A]): JsonValueCodec[T]    = wrapped(codecs, rep.decode, rep.encode)
  def valueClassKey[T, A](base: JsonKeyCodec[A], rep: ValueClassLike[T, A]): JsonKeyCodec[T]   = wrappedKey(base, rep.apply, rep.unapply)
  def convertedKey[T, A](base: JsonKeyCodec[A], rep: InstanceConverter[T, A]): JsonKeyCodec[T] = wrappedKey(base, rep.decode, rep.encode)

  private def wrapped[T, A](codecs: LocalCodecs, wrap: A => T, unwrap: T => A): JsonValueCodec[T] = new JsonValueCodec[T] {
    private def codec: JsonValueCodec[A] = codecs.get[JsonValueCodec[A]](0)

    def decodeValue(in: JsonReader, default: T): T = wrapping(in, readWith(codec, in), wrap)
    def encodeValue(x: T, out: JsonWriter): Unit   = codec.encodeValue(unwrap(x), out)
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

  def option[A](codecs: LocalCodecs): JsonValueCodec[Option[A]] = new JsonValueCodec[Option[A]] {
    def decodeValue(in: JsonReader, default: Option[A]): Option[A] = readOption(codecs.get[JsonValueCodec[A]](0), in)
    def encodeValue(x: Option[A], out: JsonWriter): Unit           = x match {
      case Some(a) => codecs.get[JsonValueCodec[A]](0).encodeValue(a, out)
      case None    => out.writeNull()
    }
    def nullValue: Option[A] = None
  }

  def iterable[A, C](codecs: LocalCodecs, factory: Factory[A, C]): JsonValueCodec[C] = new JsonValueCodec[C] {
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
    def nullValue: C = null.asInstanceOf[C]
  }

  def array[A](codecs: LocalCodecs, tag: ClassTag[A]): JsonValueCodec[Array[A]] = new JsonValueCodec[Array[A]] {
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
    def nullValue: Array[A] = null
  }

  def map[K, V, M](codecs: LocalCodecs, keys: JsonKeyCodec[K], factory: Factory[(K, V), M]): JsonValueCodec[M] = new JsonValueCodec[M] {
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
    def nullValue: M = null.asInstanceOf[M]
  }

  // indices: of the elements' codecs
  def tuple[T](codecs: LocalCodecs, indices: List[Int])(construct: Array[Any] => T): JsonValueCodec[T] = new JsonValueCodec[T] {
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

  private def readWith[A](codec: JsonValueCodec[A], in: JsonReader): A = codec.decodeValue(in, codec.nullValue)

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
