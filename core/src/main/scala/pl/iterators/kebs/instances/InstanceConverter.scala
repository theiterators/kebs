package pl.iterators.kebs.instances

import scala.reflect.{ClassTag, classTag}
import scala.util.control.NonFatal

trait InstanceConverter[Obj, Val] {
  def encode(obj: Obj): Val
  def decode(value: Val): Obj
}
object InstanceConverter {
  private def errorMessage(clazz: String, value: String, formatOpt: Option[String] = None): String = {
    s"$clazz cannot be parsed from $value".concat(formatOpt.fold("")(format => s" – should be in format $format"))
  }

  class DecodeErrorException(e: Throwable, msg: String) extends IllegalArgumentException(msg, e)

  def apply[Obj: ClassTag, Val](_encode: Obj => Val, _decode: Val => Obj, format: Option[String] = None): InstanceConverter[Obj, Val] = {
    new InstanceConverter[Obj, Val] {
      override def decode(value: Val): Obj =
        try {
          _decode(value)
        } catch {
          case NonFatal(e) =>
            throw new DecodeErrorException(e, errorMessage(classTag[Obj].runtimeClass.getName, value.toString, format))
        }

      override def encode(obj: Obj): Val = _encode(obj)
    }
  }
}
