package scalacture.experimental.immutable.deque

final class BatchedDeque[A] private (frontList: List[A], rearList: List[A]) {
  // Ensures that both front and rear are non-empty unless size <= 1.
  private[this] def balance(front: List[A], rear: List[A]): BatchedDeque[A] = (front, rear) match {
    case (Nil, r) =>
      val (left, right) = r.splitAt(r.size / 2)
      new BatchedDeque[A](right.reverse, left)
    case (f, Nil) =>
      val (left, right) = f.splitAt(f.size / 2)
      new BatchedDeque[A](left, right.reverse)
    case (f, r) => new BatchedDeque[A](f, r)
  }

  def front: A = (frontList, rearList) match {
    case (Nil, Nil) => sys.error("This Deque is empty.")
    case (Nil, x :: _) => x
    case (x :: f, _) => x
  }

  def back: A = (frontList, rearList) match {
    case (Nil, Nil) => sys.error("This Deque is empty.")
    case (x :: _, Nil) => x
    case (_, x :: _) => x
  }

  def pushFront(elem: A): BatchedDeque[A] = balance(elem :: frontList, rearList)

  def pushBack(elem: A): BatchedDeque[A] = balance(frontList, elem :: rearList)

  def popFront: (A, BatchedDeque[A]) = (frontList, rearList) match {
    case (Nil, Nil) => sys.error("This Deque is empty.")
    case (Nil, x :: _) => (x, BatchedDeque.empty)
    case (x :: fs, rs) => (x, balance(fs, rs))
  }

  def popBack: (A, BatchedDeque[A]) = (frontList, rearList) match {
    case (Nil, Nil) => sys.error("This Deque is empty.")
    case (x :: _, Nil) => (x, BatchedDeque.empty)
    case (fs, x :: rs) => (x, balance(fs, rs))
  }
}

object BatchedDeque {
  def empty[A]: BatchedDeque[A] = new BatchedDeque[A](Nil, Nil)
}
