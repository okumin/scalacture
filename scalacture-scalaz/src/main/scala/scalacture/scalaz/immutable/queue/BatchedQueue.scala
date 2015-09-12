package scalacture.scalaz.immutable.queue

import scala.collection.generic.{CanBuildFrom, SeqFactory}
import scalacture.immutable.queue.BatchedQueue

trait BatchedQueueInstances extends QueueSubInstances {
  override type XQueue[A] = BatchedQueue[A]
  override implicit def canBuildFrom[A]: CanBuildFrom[XQueue[_], A, XQueue[A]] = BatchedQueue.canBuildFrom
  override protected[this] def factory: SeqFactory[XQueue] = BatchedQueue
}

object batchedQueue extends BatchedQueueInstances
