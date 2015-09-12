package scalacture.scalaz.immutable.queue

import scalacture.immutable.queue.BatchedQueue
import scalacture.scalaz.immutable.queue.batchedQueue._
import scalaprops._
import scalaz.std.anyVal._

object BatchedQueueSpec extends Scalaprops {
  implicit def gen[A: Gen]: Gen[BatchedQueue[A]] = Gen.list[A].map(BatchedQueue(_: _*))
  implicit val cogen: Cogen[BatchedQueue[Int]] = Cogen[List[Int]].contramap(_.toList)
  implicit val fgen: Gen[BatchedQueue[Int] => Int] = Gen.f1[BatchedQueue[Int], Int]

  val queueLaws = Properties.list(
    scalazlaws.equal.all[BatchedQueue[Int]],
    scalazlaws.monoid.all[BatchedQueue[Int]],
    scalazlaws.order.all[BatchedQueue[Int]],
    scalazlaws.traverse.all[BatchedQueue],
    scalazlaws.monadPlus.all[BatchedQueue],
    scalazlaws.zip.all[BatchedQueue],
    scalazlaws.align.all[BatchedQueue],
    scalazlaws.isEmpty.all[BatchedQueue]
  )
}
