package pl.iterators.kebs.core.macros.internal

import java.util.concurrent.atomic.AtomicReference

// codecs created when they are first used, which may be while another one is created; called by generated code,
// so its binary API is kept across 3.x as the public one is
final class LocalCodecs(creators: Array[() => Any]) {
  private val codecs = Array.fill(creators.length)(new AtomicReference[AnyRef])

  def get[A](index: Int): A = {
    val codec = codecs(index)
    if (codec.get eq null) codec.compareAndSet(null, creators(index)().asInstanceOf[AnyRef])
    codec.get.asInstanceOf[A]
  }
}

object LocalCodecs {
  def apply(creators: List[() => Any]): LocalCodecs = new LocalCodecs(creators.toArray)

  def one[A](a: A): List[A]                             = a :: Nil
  def concat[A](left: List[A], right: List[A]): List[A] = left ::: right
}
