package scalacture.experimental.immutable.priorityqueue

import scala.Ordering.Implicits._
import scalacture.experimental.immutable.priorityqueue.LeftistHeap.{Branch, Leaf}

sealed abstract class LeftistHeap[A: Ordering] {
  protected def rank: Int

  final def findMin: A = this match {
    case Branch(v, _, _) => v
    case Leaf() => sys.error("This heap is empty.")
  }

  final def deleteMin(): LeftistHeap[A] = this match {
    case Branch(_, l, r) => l.meld(r)
    case Leaf() => sys.error("This heap is empty.")
  }

  final def meld(that: LeftistHeap[A]): LeftistHeap[A] = (this, that) match {
    case (Leaf(), h) => h
    case (h, Leaf()) => h
    case (Branch(v1, l1, r1), h2 @ Branch(v2, l2, r2)) if v1 <= v2 =>
      Branch.balance(v1, l1, r1.meld(h2))
    case (h1: Branch[A], Branch(v2, l2, r2)) =>
      Branch.balance(v2, l2, r2.meld(h1))
  }

  final def insert(x: A): LeftistHeap[A] = meld(Branch(x, Leaf(), Leaf()))
}

object LeftistHeap {
  def empty[A: Ordering]: LeftistHeap[A] = Leaf()

  private final case class Leaf[A: Ordering]() extends LeftistHeap[A] {
    override protected def rank: Int = 0
  }
  private final case class Branch[A: Ordering](value: A,
                                              left: LeftistHeap[A],
                                              right: LeftistHeap[A]) extends LeftistHeap[A] {
    override protected val rank: Int = right.rank + 1
  }
  private object Branch {
    // Ensures the leftist property.
    def balance[A: Ordering](value: A, a: LeftistHeap[A], b: LeftistHeap[A]): Branch[A] = {
      if (a.rank >= b.rank) Branch(value, a, b) else Branch(value, b, a)
    }
  }
}
