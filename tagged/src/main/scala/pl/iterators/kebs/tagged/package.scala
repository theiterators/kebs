package pl.iterators.kebs

import scala.language.higherKinds

package object tagged {

  /**
    * Copy of
    * https://gist.github.com/Tvaroh/a2fd772f7a66aaafc2ea48ce1fc3646d
    */
  /**
    * Adapted from
    * https://github.com/softwaremill/scala-common/blob/master/tagging/src/main/scala/com/softwaremill/tagging/package.scala
    * with added tagging operators, function-first-style tagging, and explicit container-types tagging.
    */
  type Tag[+U]        = { type Tag <: U }
  type Tagged[+T, +U] = T with Tag[U]
  type @@[+T, +U]     = Tagged[T, U]

  implicit class TaggingExtensions[T](val t: T) extends AnyVal {

    /** Tag with type `U`.
      * @tparam U type to tag with
      * @return value tagged with `U` */
    def taggedWith[U]: T @@ U = t.asInstanceOf[T @@ U]

    /** Synonym operator for `taggedWith`. */
    def @@[U]: T @@ U = taggedWith[U]
  }
  implicit class AndTaggingExtensions[T, U](val t: T @@ U) extends AnyVal {

    /** Tag tagged value with type `V`.
      * @tparam V type to tag with
      * @return value tagged with both `U` and `V` */
    def andTaggedWith[V]: T @@ (U with V) = t.asInstanceOf[T @@ (U with V)]

    /** Synonym operator for `andTaggedWith`. */
    def +@[V]: T @@ (U with V) = andTaggedWith[V]

    def map[V](f: T => V): V @@ U = f(t).taggedWith[U]
  }

  implicit class TaggingExtensionsF[F[_], T](val ft: F[T]) extends AnyVal {

    /** Tag intra-container values with type `U`.
      * @tparam U type to tag with
      * @return container with nested values tagged with `U` */
    def taggedWithF[U]: F[T @@ U] = ft.asInstanceOf[F[T @@ U]]

    /** Synonym operator for `taggedWithF`. */
    def @@@[U]: F[T @@ U] = taggedWithF[U]
  }
  implicit class AndTaggingExtensionsF[F[_], T, U](val ft: F[T @@ U]) extends AnyVal {

    /** Tag tagged intra-container values with type `U`.
      * @tparam V type to tag with
      * @return container with nested values tagged with both `U` and `V` */
    def andTaggedWithF[V]: F[T @@ (U with V)] = ft.asInstanceOf[F[T @@ (U with V)]]

    /** Synonym operator for `andTaggedWithF`. */
    def +@@[V]: F[T @@ (U with V)] = andTaggedWithF[V]
  }
}
