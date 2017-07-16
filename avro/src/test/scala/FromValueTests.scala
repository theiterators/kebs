import com.sksamuel.avro4s.FromValue
import org.scalatest.{FunSuite, Matchers}

class FromValueTests extends FunSuite with Matchers {
  import FromValueTests._
  import pl.iterators.kebs.avro._

  test("Materialize FromValue for simple value type") {
    val FromValue = implicitly[FromValue[S]]
    FromValue("hello") shouldEqual S("hello")
  }

  test("Materialize FromValue for option of simple value type") {
    val FromValue = implicitly[FromValue[Option[S]]]
    FromValue("hello") shouldEqual Some(S("hello"))
    FromValue(null) shouldEqual None
  }

  test("Materialize FromValue for compound value type") {
    import scala.collection.JavaConverters._

    val FromValue = implicitly[FromValue[V]]
    FromValue(Vector("a", "b", "c").asJava) shouldEqual V(Vector("a", "b", "c"))
  }

  test("Materialize FromValue for generic value type") {
    val FromValue = implicitly[FromValue[G[Int]]]
    FromValue(10) shouldEqual G(10)
  }

}

object FromValueTests {
  case class S(s: String)         extends AnyVal
  case class V(v: Vector[String]) extends AnyVal
  case class G[A](a: A)           extends AnyVal
}
