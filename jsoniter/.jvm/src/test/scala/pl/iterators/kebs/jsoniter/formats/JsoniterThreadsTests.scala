package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.nested.{MutA, mutA}

import java.util.concurrent.{Callable, CountDownLatch, Executors, TimeUnit}

class JsoniterThreadsTests extends JsoniterTestBase {
  object Protocol extends KebsJsoniter
  import Protocol._

  test("a codec first used by several threads at once") {
    val executor = Executors.newFixedThreadPool(8)
    try
      (1 to 200).foreach { _ =>
        val codec = implicitly[JsonValueCodec[MutA]]
        val start = new CountDownLatch(1)
        val task  = new Callable[MutA] {
          def call(): MutA = {
            start.await()
            readFromArray(writeToArray(mutA)(codec))(codec)
          }
        }
        val results = (1 to 8).map(_ => executor.submit(task))
        start.countDown()
        results.foreach(_.get(10, TimeUnit.SECONDS) shouldBe mutA)
      }
    finally executor.shutdownNow(): Unit
  }
}
