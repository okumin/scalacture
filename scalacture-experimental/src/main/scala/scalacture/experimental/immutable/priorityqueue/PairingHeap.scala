package scalacture.experimental.immutable.priorityqueue

import scala.Ordering.Implicits._
import scala.annotation.tailrec
import scalacture.experimental.immutable.priorityqueue.PairingHeap.{Branch, Leaf}

sealed abstract class PairingHeap[A: Ordering] {
  final def findMin: A = this match {
    case Branch(v, _) => v
    case Leaf() => sys.error("This heap is empty.")
  }

  final def deleteMin(): PairingHeap[A] = {
    @tailrec
    def mergePairs(heaps: List[Branch[A]], paired: List[PairingHeap[A]]): List[PairingHeap[A]] = {
      heaps match {
        case Nil => paired
        case h :: Nil => h :: paired
        case h1 :: h2 :: hs => mergePairs(hs, h1.meld(h2) :: paired)
      }
    }

    this match {
      case Leaf() => sys.error("This heap is empty.")
      case Branch(_, Nil) => Leaf()
      case Branch(_, heaps) =>
        mergePairs(heaps, Nil).foldLeft[PairingHeap[A]](Leaf()) { (acc, pair) => acc.meld(pair) }
    }
  }

  final def meld(that: PairingHeap[A]): PairingHeap[A] = (this, that) match {
    case (Leaf(), h) => h
    case (h, Leaf()) => h
    case (Branch(v1, hs1), h2 @ Branch(v2, _)) if v1 <= v2 => Branch(v1, h2 :: hs1)
    case (h1: Branch[A], Branch(v2, hs2)) => Branch(v2, h1 :: hs2)
  }

  final def insert(x: A): PairingHeap[A] = meld(Branch(x, Nil))
}

object PairingHeap {
  def empty[A: Ordering]: PairingHeap[A] = Leaf()

  private final case class Leaf[A: Ordering]() extends PairingHeap[A]

  private final case class Branch[A: Ordering](value: A,
                                               heaps: List[Branch[A]]) extends PairingHeap[A]
}
