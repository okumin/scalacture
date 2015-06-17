package scalacture.immutable.queue

import scala.collection.generic.SeqFactory

class BatchedQueueSpec extends QueueSpec[BatchedQueue] {
  override protected[this] def factory: SeqFactory[BatchedQueue] = BatchedQueue

  "A BatchedQueue" should {
    "return BatchedQueue" when {
      "enqueueing" in {
        forAll { elem: Int =>
          val actual: BatchedQueue[Int] = BatchedQueue.empty[Int].enqueue(elem)
          assert(actual === BatchedQueue(elem))
        }
      }
    }
  }
}
