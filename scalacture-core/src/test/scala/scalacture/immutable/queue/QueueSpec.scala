package scalacture.immutable.queue

import org.scalacheck.{Arbitrary, Gen}
import scala.collection.generic.{CanBuildFrom, GenericTraversableTemplate, SeqFactory}
import scala.language.higherKinds
import scalacture.helper.SeqSpec

abstract class QueueSpec[CC[X] <: Queue[X] with QueueLike[X, CC[X]] with GenericTraversableTemplate[X, CC]](implicit private[this] val seqCBF: CanBuildFrom[CC[_], Int, CC[Int]])
  extends SeqSpec[Int, CC] {

  protected[this] sealed abstract class Command
  protected[this] case object Peek extends Command
  protected[this] case class Enqueue(x: Int) extends Command
  protected[this] case object Dequeue extends Command
  protected[this] object Command {
    implicit val arbitrary: Arbitrary[Command] = Arbitrary {
      Gen.oneOf[Command](Peek, Arbitrary.arbitrary[Int].map(Enqueue), Dequeue)
    }
  }

  "A Queue" should {
    "behave like a FIFO queue" in {
      forAll { (elems: List[Int], commands: List[Command]) =>
        val zero = (factory(elems: _*): Queue[Int], scala.collection.immutable.Queue(elems: _*))
        val (q, m) = commands.foldLeft(zero) {
          case ((queue, model), Peek) if queue.isEmpty && model.isEmpty =>
            (queue, model)
          case ((queue, model), Peek) =>
            assert(queue.peek === model.front)
            (queue, model)
          case ((queue, model), Enqueue(x)) =>
            val newQueue = queue.enqueue(x)
            val newModel = model.enqueue(x)
            assert(newQueue.size === newModel.size)
            assert(newQueue.isEmpty === newModel.isEmpty)
            assert(newQueue === newModel)
            (newQueue, newModel)
          case ((queue, model), Dequeue) if queue.isEmpty && model.isEmpty =>
            (queue, model)
          case ((queue, model), Dequeue) =>
            val (qx, newQueue) = queue.dequeue
            val (mx, newModel) = model.dequeue
            assert(qx === mx)
            assert(newQueue.size === newModel.size)
            assert(newQueue.isEmpty === newModel.isEmpty)
            assert(newQueue === newModel)
            (newQueue, newModel)
        }
        assert(q === m)
      }
    }

    "have correct elements and maintain FIFO order" when {
      "enqueueing" in {
        forAll { elems: List[Int] =>
          val actual = elems.foldLeft(factory.empty[Int]: Queue[Int]) { (queue, elem) =>
            queue.enqueue(elem)
          }
          assert(actual === elems)
          assert(actual.length === elems.size)
        }
      }

      "dequeueing" in {
        forAll(elemsAndIndex) {
          case (elems, times) =>
            val queue = factory(elems: _*)
            val (_dequeued, rest) = (1 to times).foldLeft((List.empty[Int], queue)) {
              case ((list, q), _) =>
                q.dequeueOption match {
                  case None => (list, q)
                  case Some((x, qq)) => (x :: list, qq)
                }
            }
            val dequeued = _dequeued.reverse
            assert(dequeued === elems.take(times))
            assert(rest === elems.drop(times))
            assert(rest.length === elems.drop(times).size)
            assert(dequeued ::: rest.toList === elems)
        }
      }
    }

    "retrieve the first element" in {
      forAll(Gen.nonEmptyListOf(Arbitrary.arbitrary[Int])) { elems: List[Int] =>
        val queue = factory(elems: _*)
        assert(queue.head === elems.head)
        assert(queue.tail === elems.tail)
        assert(queue.peek === elems.head)
        assert(queue.peekOption === Some(elems.head))
        assert(queue.dequeue === (elems.head, elems.tail))
        assert(queue.dequeueOption === Some((elems.head, elems.tail)))
      }
    }

    "not be able to dequeue" when {
      "the queue is empty" in {
        val empty = factory[Int]()
        intercept[NoSuchElementException] { empty.peek }
        assert(empty.peekOption === None)
        intercept[NoSuchElementException] { empty.dequeue }
        assert(empty.dequeueOption === None)
      }
    }
  }
}

class DefaultQueueSpec extends QueueSpec[Queue] {
  override protected[this] def factory: SeqFactory[Queue] = Queue
}
