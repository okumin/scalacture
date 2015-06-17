package scalacture.immutable.queue

import scala.collection.generic.{CanBuildFrom, GenericCompanion, GenericTraversableTemplate, SeqFactory}
import scala.collection.mutable.ListBuffer
import scala.collection.{GenTraversableOnce, mutable}
import scalacture.immutable.queue.HoodMelvilleQueue.{Appending, Done, Idle, Reversing, RotationState}

/**
 * A queue implemented by Hood and Melville.
 *
 * This is a immutable and real-time queue based on global rebuilding.
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
 *     <td>O(1)</td>
 *     <td>O(1)</td>
 *     <td>O(1)</td>
 *   </tr>
 * </table>
 */
@SerialVersionUID(1067800614785502062L)
final class HoodMelvilleQueue[+A] private (diff: Int,
                                           working: List[A],
                                           state: RotationState[A],
                                           rearList: List[A])
  extends Queue[A]
  with QueueLike[A, HoodMelvilleQueue[A]]
  with GenericTraversableTemplate[A, HoodMelvilleQueue]
  with Serializable {

  /**
   * A rotation starts when the length of the front list becomes one longer than that of the rear list.
   * A front list means a working copy on starting rotation.
   * And the rotation finishes when `exec` or `invalidate` are called 2m + 2 times,
   * where m is the initial length of the working copy.
   */
  private[this] def exec[B >: A](state: RotationState[B]): RotationState[B] = state match {
    case Reversing(ok, x :: xs, fs, y :: ys, rs) => Reversing(ok + 1, xs, x :: fs, ys, y :: rs)
    case Reversing(ok, Nil, fs, y :: Nil, rs) => Appending(ok, fs, y :: rs)
    case Appending(0, _, rs) => Done(rs)
    case Appending(ok, f :: fs, rs) => Appending(ok - 1, fs, f :: rs)
    case s => s
  }

  private[this] def invalidate(state: RotationState[A]): RotationState[A] = state match {
    case Reversing(ok, xs, fs, ys, rs) => Reversing(ok - 1, xs, fs, ys, rs)
    case Appending(0, _, r :: rs) => Done(rs)
    case Appending(ok, fs, rs) => Appending(ok - 1, fs, rs)
    case s => s
  }

  /**
   * If the reverse of the front list is completed,
   * the true diff is achieved by the following calculation.
   *
   * Where `m` is the initial length of the working copy and the total number of times of `exec`,
   *       `x` is the total number of times of that `enqueue` or `dequeue` are executed,
   *       `d` is the true diff that represents the difference
   *           between the length of the front list and that of the rear list.
   *
   * Since the initial `diff` is -1 and then `diff` decreases by 1 per `enqueue` or `dequeue`,
   * diff = -x - 1
   * x = -diff - 1
   * Since `exec` are called twice at the beginning of each rotation,
   * m = x + 2
   *   = -diff - 1 + 2
   *   = -diff + 1
   * Since `2m + 1` means the initial true diff
   * and then the true diff decreases by 1 per `enqueue` or `dequeue`,
   * d = 2m + 1 - x
   *   = 2(1 - diff) + 1 - (-diff - 1)
   *   = 4 - diff
   */
  private[this] def execute[B >: A](diff: Int,
                                    working: List[B],
                                    state: RotationState[B],
                                    rear: List[B]): HoodMelvilleQueue[B] = {
    exec(state) match {
      case s @ Reversing(_, Nil, _, _ :: Nil, _) => new HoodMelvilleQueue[B](4 - diff, working, s, rear)
      case Done(xs) => new HoodMelvilleQueue[B](diff, xs, Idle, rear)
      case newState => new HoodMelvilleQueue[B](diff, working, newState, rear)
    }
  }

  /**
   * Where m is the initial length of the working copy on starting a rotation,
   * it is the following conditions to start the next rotation.
   *
   * - after 2m + 2 calls to `enqueue` or `dequeue`
   *   because the length of the front list becomes longer than that of the rear list.
   * - after m calls to `dequeue` because the working copy becomes empty
   *
   * `exec` are called at the following occasion.
   *
   * - called twice at the beginning of each rotation
   * - called once for every `enqueue` or `dequeue`
   *
   * After `enqueue` or `dequeue` are called 2m + 2, `exec` has been called 2m + 4 times.
   * After `dequeue` is called m times,
   * `exec` has been called m + 2 times and `invalidate` has been called m times.
   *
   * Thus, a rotation always finishes before the next rotation starts.
   */
  private[this] def check[B >: A](diff: Int,
                                  working: List[B],
                                  state: RotationState[B],
                                  rear: List[B]): HoodMelvilleQueue[B] = {
    (diff, working, state, rear) match {
      case (-1, ws, Idle, r :: Nil) => new HoodMelvilleQueue[B](1, r :: Nil, Idle, Nil)
      case (-1, w :: Nil, Idle, r1 :: r2 :: Nil) => new HoodMelvilleQueue[B](3, w :: r2 :: r1 :: Nil, Idle, Nil)
      case (-1, ws, Idle, rs) => execute(-1, ws, exec(Reversing(0, ws, Nil, rs, Nil)), Nil)
      case (d, ws, s, rs) => execute(d, ws, s, rs)
    }
  }

  override def companion: GenericCompanion[HoodMelvilleQueue] = HoodMelvilleQueue

  override def isEmpty: Boolean = working.isEmpty

  override def ++[B >: A, That](that: GenTraversableOnce[B])(implicit bf: CanBuildFrom[HoodMelvilleQueue[A], B, That]): That = {
    bf match {
      case _: HoodMelvilleQueue.GenericCanBuildFrom[_] =>
        that.foldLeft[HoodMelvilleQueue[B]](this) { (q, x) => q.enqueue(x) }.asInstanceOf[That]
      case _ => super.++(that)
    }
  }

  /**
   * Returns the first element.
   * O(1) time.
   */
  override def peek: A = working match {
    case Nil => throw new NoSuchElementException("This Queue is empty.")
    case x :: _ => x
  }

  /**
   * Creates a new queue with element added at the end of the old queue.
   * O(1) time.
   * @param elem the element to insert.
   * @return a new queue with the inserted element.
   */
  override def enqueue[B >: A](elem: B): HoodMelvilleQueue[B] = {
    check(diff - 1, working, state, elem :: rearList)
  }

  /**
   * Returns a tuple with the first element in the queue, and a new queue with this element removed.
   * O(1) time.
   * @return the first element of the queue and a new queue.
   */
  override def dequeue: (A, HoodMelvilleQueue[A]) = working match {
    case Nil => throw new NoSuchElementException("This Queue is empty.")
    case x :: xs => (x, check(diff - 1, xs, invalidate(state), rearList))
  }
}

object HoodMelvilleQueue extends SeqFactory[HoodMelvilleQueue] {
  private sealed abstract class RotationState[+A]
  private case class Reversing[A](ok: Int,
                                  xs: List[A],
                                  fs: List[A],
                                  ys: List[A],
                                  rs: List[A]) extends RotationState[A]
  private case class Appending[A](ok: Int, fs: List[A], rs: List[A]) extends RotationState[A]
  private case class Done[A](xs: List[A]) extends RotationState[A]
  private case object Idle extends RotationState[Nothing]

  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, HoodMelvilleQueue[A]] = {
    ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  }

  override def newBuilder[A]: mutable.Builder[A, HoodMelvilleQueue[A]] = {
    ListBuffer.empty[A].mapResult { xs => new HoodMelvilleQueue[A](xs.size, xs, Idle, Nil) }
  }
}
