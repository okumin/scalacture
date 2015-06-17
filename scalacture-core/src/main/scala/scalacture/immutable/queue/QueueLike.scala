package scalacture.immutable.queue

import scala.collection.LinearSeqOptimized
import scala.collection.generic.CanBuildFrom

trait QueueLike[+A, +This <: Queue[A] with QueueLike[A, This]]
  extends LinearSeqOptimized[A, This] { self: This =>

  final override def :+[B >: A, That](elem: B)(implicit bf: CanBuildFrom[This, B, That]): That = {
    bf match {
      case _: Queue.GenericCanBuildFrom[_] => enqueue(elem).asInstanceOf[That]
      case _ => super.:+(elem)
    }
  }

  final override def head: A = peek

  final override def tail: This = dequeueOption match {
    case Some((_, t)) => t
    case None => throw new UnsupportedOperationException("tail of empty queue")
  }

  override def isEmpty: Boolean

  /**
   * Returns the first element.
   */
  def peek: A

  /**
   * Returns the first element or None if the queue is empty.
   */
  final def peekOption: Option[A] = if (isEmpty) None else Some(peek)

  /**
   * Creates a new queue with element added at the end of the old queue.
   * @param elem the element to insert.
   * @return a new queue with the inserted element.
   */
  def enqueue[B >: A](elem: B): Queue[B]

  /**
   * Returns a tuple with the first element in the queue, and a new queue with this element removed.
   * @return the first element of the queue and a new queue.
   */
  def dequeue: (A, This)

  /**
   * Returns a tuple with the first element in the queue, and a new queue with this element removed.
   * @return the first element of the queue and a new queue or None if the queue is empty.
   */
  final def dequeueOption: Option[(A, This)] = if (isEmpty) None else Some(dequeue)
}
