package pl.iterators.kebs.examples

import java.util.UUID
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta.tagged

@tagged object TaggedMeta {

  trait UserIdTag
  trait ListTag
  trait PolyTag[+A]
  trait TestNotInfixTag

  type IntUserId  = Int @@ UserIdTag
  type UUIDUserId = UUID @@ UserIdTag

  type PolymorphicTaggedType[A] = List[A] @@ ListTag
  type TestWithPolyTag[A]       = Int @@ PolyTag[A]

  type TestNotInfix = @@[String, TestNotInfixTag]

  // Tagged types can have objects named the same
  object IntUserId {
    def foo(): String = "bar"
  }

  // These objects can contain `validate` methods
  object TestNotInfix {
    def validate(arg: String) = Either.cond(arg.length > 20, arg, "That's not a valid string")
  }

  // If an object contains `validate` method, it is later used in `from` and `apply` methods. `from` returns
  // an Either and `apply` throws an exception if the result of `validate` is a Left.
  assert(TestNotInfix.from("too short").isLeft)

  // Tag types can also have objects with the same name
  object UserIdTag {
    def foo(): String = "bar"
  }

  TestWithPolyTag[Int](42)
  PolymorphicTaggedType(List(1, 2, 3))

}
