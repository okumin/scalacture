package scalacture.scalaz.immutable.queue

import scalacture.immutable.queue.Queue
import scalacture.scalaz.immutable.queue.queue._
import scalaprops._
import scalaz.std.anyVal._

object QueueSpec extends Scalaprops {
  implicit def gen[A: Gen]: Gen[Queue[A]] = Gen.list[A].map(Queue(_: _*))
  implicit val cogen: Cogen[Queue[Int]] = Cogen[List[Int]].contramap(_.toList)
  implicit val fgen: Gen[Queue[Int] => Int] = Gen.f1[Queue[Int], Int]

  val queueLaws = Properties.list(
    scalazlaws.equal.all[Queue[Int]],
    scalazlaws.monoid.all[Queue[Int]],
    scalazlaws.order.all[Queue[Int]],
    scalazlaws.traverse.all[Queue],
    scalazlaws.monadPlus.all[Queue],
    scalazlaws.zip.all[Queue],
    scalazlaws.align.all[Queue],
    scalazlaws.isEmpty.all[Queue]
  )
}
