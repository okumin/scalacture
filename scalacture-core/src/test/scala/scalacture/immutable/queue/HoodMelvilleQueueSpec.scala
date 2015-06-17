package scalacture.immutable.queue

import scala.collection.generic.SeqFactory

class HoodMelvilleQueueSpec extends QueueSpec[HoodMelvilleQueue] {
  override protected[this] def factory: SeqFactory[HoodMelvilleQueue] = HoodMelvilleQueue

  "A HoodMelvilleQueue" should {
    "return HoodMelvilleQueue" when {
      "enqueueing" in {
        forAll { elem: Int =>
          val actual: HoodMelvilleQueue[Int] = HoodMelvilleQueue.empty[Int].enqueue(elem)
          assert(actual === HoodMelvilleQueue(elem))
        }
      }
    }
  }
}
