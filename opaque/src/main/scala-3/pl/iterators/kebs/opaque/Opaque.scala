package pl.iterators.kebs.opaque

import pl.iterators.kebs.macros.ValueClassLike

trait Opaque[OpaqueType, Unwrapped](using ev: OpaqueType =:= Unwrapped) {
  /**
    * Validates and transforms (ex. sanitizes) unwrapped value.
    * @param unwrapped value to be validated and transformed
    * @return Left(reason) if validation fails; Right(opaqueType) if validation succeeds
    */
  protected def validate(unwrapped: Unwrapped): Either[String, OpaqueType] = Right(ev.flip.apply(unwrapped))

  /**
    * Validates and transforms (ex. sanitizes) unwrapped value. By default, there is no validation or transformation.
    * @param unwrapped value to be validated and transformed
    * @return Left(reason) if validation fails; Right(opaqueType) if validation succeeds
    */
  def from(unwrapped: Unwrapped): Either[String, OpaqueType] = validate(unwrapped)

  /**
    * Creates an instance of OpaqueType from unwrapped value.
    * @param unwrapped value to be validated, transformed and wrapped in OpaqueType
    * @throws IllegalArgumentException with reason, if validation fails
    * @return OpaqueType wrapping validated & transformed unwrapped value
    */
  def apply(unwrapped: Unwrapped): OpaqueType = validate(unwrapped).fold(l => throw new IllegalArgumentException(l), identity)

  /**
    * Creates an instance of OpaqueType from unwrapped value in an unsafe manner - without validation or transformation.
    * @param unwrapped value to be wrapped in OpaqueType
    * @return OpaqueType wrapping unwrapped value
    */
  def unsafe(unwrapped: Unwrapped): OpaqueType = ev.flip.apply(unwrapped)

  extension (w: OpaqueType) {
    /**
      * Unwraps value wrapped in OpaqueType.
      * @return unwrapped value
      */
    def unwrap: Unwrapped = ev.apply(w)
  }

  given vcLike: ValueClassLike[OpaqueType, Unwrapped] = ValueClassLike(apply, _.unwrap)
}