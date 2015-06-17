package scalacture.immutable.queue

import scala.collection.generic.{CanBuildFrom, GenericCompanion, GenericTraversableTemplate, SeqFactory}
import scala.collection.{immutable, mutable}

/**
 * A FIFO queue.
 */
trait Queue[+A]
  extends QueueLike[A, Queue[A]]
  with immutable.LinearSeq[A]
  with GenericTraversableTemplate[A, Queue] {

  override def companion: GenericCompanion[Queue] = Queue

  override def toString(): String = mkString(stringPrefix + "(", ", ", ")")
}

object Queue extends SeqFactory[Queue] {
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, Queue[A]] = {
    ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  }

  override def newBuilder[A]: mutable.Builder[A, Queue[A]] = BatchedQueue.newBuilder
}
