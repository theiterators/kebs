package pl.iterators.kebs

/**
  * Adapted from shapeless' implementation
  * https://github.com/milessabin/shapeless/blob/master/core/src/main/scala/shapeless/typeoperators.scala
  */
package object tag {
  trait Tagged[+U]
  type @@[+T, +U] = T with Tagged[U]

  implicit class Tagger[T](private val t: T) extends AnyVal {
    @inline def taggedWith[U]: T @@ U = t.asInstanceOf[T @@ U]
    @inline def @@[U]: T @@ U         = taggedWith[U]
  }

  implicit class AndTagger[T, U](private val t: T @@ U) extends AnyVal {
    @inline def andTaggedWith[V]: T @@ (U with V) = t.asInstanceOf[T @@ (U with V)]
    @inline def +@[V]: T @@ (U with V)            = andTaggedWith[V]

    @inline def map[V](f: T => V): V @@ U = f(t).taggedWith[U]
  }
}
