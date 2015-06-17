package scalacture.immutable.queue

import scala.collection.generic.{CanBuildFrom, GenericCompanion, GenericTraversableTemplate, SeqFactory}
import scala.collection.mutable.ListBuffer
import scala.collection.{GenTraversableOnce, mutable}

/**
 * A queue made up of a pair of lists.
 *
 * This data structure achieves good amortized efficiency by batched rebuilding.
 * However persistently used, amortized bounds of `dequeue` degrade to the worst-case bounds of that.
 *
 * <table>
 *   <tr>
 *     <td></td>
 *     <td>worst-case</td>
 *     <td>amortized</td>
 *     <td>under persistent usage</td>
 *   </tr>
 *
 *   <tr>
 *     <td>peek</td>
 *     <td>O(1)</td>
 *     <td>O(1)</td>
 *     <td>O(1)</td>
 *   </tr>
 *
 *   <tr>
 *     <td>enqueue</td>
 *     <td>O(1)</td>
 *     <td>O(1)</td>
 *     <td>O(1)</td>
 *   </tr>
 *
 *   <tr>
 *     <td>dequeue</td>
 *     <td>O(n)</td>
 *     <td>O(1)</td>
 *     <td>O(n)</td>
 *   </tr>
 * </table>
 */
@SerialVersionUID(1382407360619706277L)
final class BatchedQueue[+A] private (frontList: List[A], rearList: List[A])
  extends Queue[A]
  with QueueLike[A, BatchedQueue[A]]
  with GenericTraversableTemplate[A, BatchedQueue]
  with Serializable {

  // Ensures that frontList is non-empty unless the queue is empty.
  private[this] def rebuild[B >: A](front: List[B], rear: List[B]): BatchedQueue[B] = {
    (front, rear) match {
      case (Nil, r) => new BatchedQueue[B](r.reverse, Nil)
      case (f, r) => new BatchedQueue[B](f, r)
    }
  }

  override def companion: GenericCompanion[BatchedQueue] = BatchedQueue

  override def length: Int = frontList.size + rearList.size

  override def isEmpty: Boolean = frontList.isEmpty && rearList.isEmpty

  override def ++[B >: A, That](that: GenTraversableOnce[B])(implicit bf: CanBuildFrom[BatchedQueue[A], B, That]): That = {
    bf match {
      case _: BatchedQueue.GenericCanBuildFrom[_] =>
        rebuild(frontList, that.toList.reverse ::: rearList).asInstanceOf[That]
      case _ => super.++(that)
    }
  }

  override def ++:[B >: A, That](that: Traversable[B])(implicit bf: CanBuildFrom[BatchedQueue[A], B, That]): That = {
    bf match {
      case _: BatchedQueue.GenericCanBuildFrom[_] =>
        new BatchedQueue[B](that.toList ::: frontList, rearList).asInstanceOf[That]
      case _ => super.++:(that)
    }
  }

  override def +:[B >: A, That](elem: B)(implicit bf: CanBuildFrom[BatchedQueue[A], B, That]): That = {
    bf match {
      case _: BatchedQueue.GenericCanBuildFrom[_] =>
        new BatchedQueue[B](elem :: frontList, rearList).asInstanceOf[That]
      case _ => super.+:(elem)
    }
  }

  override def reverse: BatchedQueue[A] = rebuild(rearList, frontList)

  /**
   * Returns the first element.
   * O(1) time.
   */
  override def peek: A = frontList match {
    case Nil => throw new NoSuchElementException("This Queue is empty.")
    case x :: _ => x
  }

  /**
   * Creates a new queue with element added at the end of the old queue.
   * O(1) time.
   * @param elem the element to insert.
   * @return a new queue with the inserted element.
   */
  override def enqueue[B >: A](elem: B): BatchedQueue[B] = {
    rebuild(frontList, elem :: rearList)
  }

  /**
   * Returns a tuple with the first element in the queue, and a new queue with this element removed.
   * O(n) worst-case time, O(1) amortized time.
   * @return the first element of the queue and a new queue.
   */
  override def dequeue: (A, BatchedQueue[A]) = (frontList, rearList) match {
    case (Nil, _) => throw new NoSuchElementException("This Queue is empty.")
    case (x :: f, r) => (x, rebuild(f, r))
  }
}

object BatchedQueue extends SeqFactory[BatchedQueue] {
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, BatchedQueue[A]] = {
    ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  }

  override def newBuilder[A]: mutable.Builder[A, BatchedQueue[A]] = {
    ListBuffer.empty[A].mapResult { xs => new BatchedQueue[A](xs, Nil) }
  }
}
