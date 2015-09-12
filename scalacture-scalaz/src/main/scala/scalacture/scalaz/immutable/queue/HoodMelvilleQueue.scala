package scalacture.scalaz.immutable.queue

import scala.collection.generic.{CanBuildFrom, SeqFactory}
import scalacture.immutable.queue.HoodMelvilleQueue

trait HoodMelvilleQueueInstances extends QueueSubInstances {
  override type XQueue[A] = HoodMelvilleQueue[A]
  override protected[this] def factory: SeqFactory[XQueue] = HoodMelvilleQueue
  override implicit def canBuildFrom[A]: CanBuildFrom[XQueue[_], A, XQueue[A]] = HoodMelvilleQueue.canBuildFrom
}

object hoodMelvilleQueue extends HoodMelvilleQueueInstances
