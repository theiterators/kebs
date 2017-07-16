import com.sksamuel.avro4s.ToValue
import org.scalatest.{FunSuite, Matchers}

class ToValueTests extends FunSuite with Matchers {
  import ToValueTests._
  import pl.iterators.kebs.avro._

  test("Materialize ToValue for simple value type") {
    val ToValue = implicitly[ToValue[S]]
    ToValue(S("hello")) shouldEqual "hello"
  }

  test("Materialize ToValue for option of value type") {
    val ToValue = implicitly[ToValue[Option[S]]]

    ToValue(Some(S("hello"))) shouldEqual "hello"
  }

  test("Materialize ToValue for compound value type") {
    import scala.collection.JavaConverters._

    val ToValue = implicitly[ToValue[V]]
    ToValue(V(Vector("a", "b", "c"))) shouldEqual Vector("a", "b", "c").asJava
  }

  test("Materialize ToValue for generic value type") {
    val ToValue = implicitly[ToValue[G[Int]]]
    ToValue(G(10)) shouldEqual 10
  }

  case class NotAnyVal(i: Int)
  test("Do not materialize ToValue for ref type") {
    val ToValue = implicitly[ToValue[NotAnyVal]]
    ToValue(NotAnyVal(10)) should not equal 10
  }

}

object ToValueTests {
  case class S(s: String)         extends AnyVal
  case class V(v: Vector[String]) extends AnyVal
  case class G[A](a: A)           extends AnyVal
}
