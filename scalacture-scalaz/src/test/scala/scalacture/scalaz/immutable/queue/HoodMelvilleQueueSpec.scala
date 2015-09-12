package scalacture.scalaz.immutable.queue

import scalacture.immutable.queue.HoodMelvilleQueue
import scalacture.scalaz.immutable.queue.hoodMelvilleQueue._
import scalaprops._
import scalaz.std.anyVal._

object HoodMelvilleQueueSpec extends Scalaprops {
  implicit def gen[A: Gen]: Gen[HoodMelvilleQueue[A]] = Gen.list[A].map(HoodMelvilleQueue(_: _*))
  implicit val cogen: Cogen[HoodMelvilleQueue[Int]] = Cogen[List[Int]].contramap(_.toList)
  implicit val fgen: Gen[HoodMelvilleQueue[Int] => Int] = Gen.f1[HoodMelvilleQueue[Int], Int]

  val queueLaws = Properties.list(
    scalazlaws.equal.all[HoodMelvilleQueue[Int]],
    scalazlaws.monoid.all[HoodMelvilleQueue[Int]],
    scalazlaws.order.all[HoodMelvilleQueue[Int]],
    scalazlaws.traverse.all[HoodMelvilleQueue],
    scalazlaws.monadPlus.all[HoodMelvilleQueue],
    scalazlaws.zip.all[HoodMelvilleQueue],
    scalazlaws.align.all[HoodMelvilleQueue],
    scalazlaws.isEmpty.all[HoodMelvilleQueue]
  )
}
