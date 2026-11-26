package pl.iterators.kebs.core.macros

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.internal.JsonDerivationState

import scala.util.Try

class JsonDerivationStateTests extends AnyFunSuite with Matchers {
  private object Abort extends RuntimeException

  private def entryPoint[A](body: => A): A = JsonDerivationState.entryPoint(_ eq Abort)(body)

  // the compiler's implicit search: a candidate that throws is not an instance
  private def search(candidate: => Boolean): Boolean = Try(candidate).getOrElse(false)

  test("a crash in an entry point reaches the derivation whose search swallowed it") {
    val crash = the[RuntimeException] thrownBy JsonDerivationState.discovering(search(entryPoint(sys.error("boom"))))
    crash.getMessage shouldBe "boom"
  }

  test("a crash in a nested search reaches the root") {
    a[RuntimeException] should be thrownBy JsonDerivationState.inOneScope {
      JsonDerivationState.discovering(search(entryPoint(JsonDerivationState.exists("Nested")(search(entryPoint(sys.error("boom")))))))
    }
  }

  test("an abort only fails the search") {
    JsonDerivationState.discovering(search(entryPoint(throw Abort)))._1 shouldBe false
  }

  test("the state is restored after a crash") {
    Try(JsonDerivationState.discovering(JsonDerivationState.searching("owner")(entryPoint(sys.error("boom")))))
    JsonDerivationState.isDiscovering shouldBe false
    JsonDerivationState.isSearching shouldBe false
    JsonDerivationState.discovering(true)._1 shouldBe true
  }

  test("within one scope a question is answered once") {
    var searches       = 0
    def ask(): Boolean = JsonDerivationState.exists("Codec") { searches += 1; true }
    JsonDerivationState.inOneScope {
      ask() shouldBe true
      ask() shouldBe true
      searches shouldBe 1
    }
    ask() shouldBe true
    searches shouldBe 2
  }

  test("a definition outside the search that the compiler types during it is a root of its own") {
    JsonDerivationState.discovering {
      JsonDerivationState.searching("owner") {
        JsonDerivationState.entering(_ == "owner")(JsonDerivationState.isDiscovering) shouldBe true
        JsonDerivationState.entering(_ == "another owner") {
          JsonDerivationState.isDiscovering shouldBe false
          JsonDerivationState.isSearching shouldBe false
        }
        JsonDerivationState.isDiscovering shouldBe true
      }
    }
  }

  test("the requests of a search are those made during it") {
    val request = JsonDerivationState.Request("Codec", "Codec Item", "Item")
    JsonDerivationState
      .discovering {
        JsonDerivationState.request(request)
        JsonDerivationState.discovering(true)._2.toList shouldBe Nil
      }
      ._2
      .toList shouldBe List(request)
  }

  test("an instance that needs itself does not exist") {
    JsonDerivationState.exists("Codec")(JsonDerivationState.exists("Codec")(true)) shouldBe false
  }
}
