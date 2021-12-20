import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

object EquivSupportSpecification extends Properties("EquivSupport") {

  import StringsDomain._
  import pl.iterators.kebs.support._

  private def areEquiv[A](e1: A, e2: A)(implicit E: Equiv[A]): Boolean =
    E.equiv(e1, e2)

  implicit private val equiv: Equiv[String] = Equiv.reference[String]

  property("tagged string should be equivalent to reference of non tagged string") = forAll { stringValue: String =>
    val string       = new String(stringValue)
    val stringTagged = TaggedString(string)

    areEquiv(stringTagged, TaggedString(string))
  }

  property("tagged string should not be equivalent to new instance of non tagged string") = forAll { stringValue: String =>
    val string       = new String(stringValue)
    val stringTagged = TaggedString(string)

    !areEquiv(stringTagged, TaggedString(new String(stringValue)))
  }

  property("boxed string should be equivalent to reference of non boxed string") = forAll { stringValue: String =>
    val string      = new String(stringValue)
    val stringBoxed = BoxedString(string)

    areEquiv(stringBoxed, BoxedString(string))
  }

  property("boxed string should not be equivalent to new instance of non boxed string") = forAll { stringValue: String =>
    val string      = new String(stringValue)
    val stringBoxed = BoxedString(string)

    !areEquiv(stringBoxed, BoxedString(new String(stringValue)))
  }
}
