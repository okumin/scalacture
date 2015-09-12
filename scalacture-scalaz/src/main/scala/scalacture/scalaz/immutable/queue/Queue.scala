package scalacture.scalaz.immutable.queue

import scala.annotation.tailrec
import scala.collection.generic.{CanBuildFrom, GenericTraversableTemplate, SeqFactory}
import scala.language.higherKinds
import scalacture.immutable.queue.{Queue, QueueLike}
import scalaz._

trait QueueSub {
  type XQueue[A] <: Queue[A] with QueueLike[A, XQueue[A]] with GenericTraversableTemplate[A, XQueue]
  implicit def canBuildFrom[A]: CanBuildFrom[XQueue[_], A, XQueue[A]]
  protected[this] def factory: SeqFactory[XQueue]
}

trait QueueSubInstances0 extends QueueSub {
  implicit def queueEqual[A: Equal]: Equal[XQueue[A]] = new Equal[XQueue[A]] {
    override def equalIsNatural: Boolean = Equal[A].equalIsNatural
    override def equal(a1: XQueue[A], a2: XQueue[A]): Boolean = a1.corresponds(a2)(Equal[A].equal)
  }
}

trait QueueSubInstances extends QueueSubInstances0 {
  implicit val queueInstance = {
    new Traverse[XQueue]
      with MonadPlus[XQueue]
      with Zip[XQueue]
      with Unzip[XQueue]
      with Align[XQueue]
      with IsEmpty[XQueue] {

      override def traverseImpl[G[_], A, B](fa: XQueue[A])(f: A => G[B])(implicit F: Applicative[G]): G[XQueue[B]] = {
        fa.foldLeft(F.point(empty[B])) { (fbs, a) =>
          F.apply2(fbs, f(a))(_ :+ _)
        }
      }

      override def zip[A, B](a: => XQueue[A], b: => XQueue[B]): XQueue[(A, B)] = a match {
        case x if x.isEmpty => empty[(A, B)]
        case x => x.zip(b)
      }

      override def unzip[A, B](a: XQueue[(A, B)]): (XQueue[A], XQueue[B]) = a.unzip

      override def alignWith[A, B, C](f: A \&/ B => C): (XQueue[A], XQueue[B]) => XQueue[C] = {
        (as, bs) =>
          val sizeA = as.size
          val sizeB = bs.size
          (as, bs).zipped.map((a, b) => f(\&/.Both(a, b))) ++ {
            if (sizeA > sizeB) as.drop(sizeB).map(a => f(\&/.This(a)))
            else bs.drop(sizeA).map(b => f(\&/.That(b)))
          }
      }

      override def isEmpty[A](fa: XQueue[A]): Boolean = fa.isEmpty

      override def empty[A]: XQueue[A] = factory.empty[A]

      override def bind[A, B](fa: XQueue[A])(f: (A) => XQueue[B]): XQueue[B] = fa.flatMap(f)

      override def point[A](a: => A): XQueue[A] = factory(a)

      override def plus[A](a: XQueue[A], b: => XQueue[A]): XQueue[A] = a ++ b
    }
  }

  implicit def queueMonoid[A]: Monoid[XQueue[A]] = new Monoid[XQueue[A]] {
    override def zero: XQueue[A] = factory.empty[A]
    override def append(f1: XQueue[A], f2: => XQueue[A]): XQueue[A] = f1 ++ f2
  }

  implicit def queueShow[A: Show]: Show[XQueue[A]] = new Show[XQueue[A]] {
    override def show(f: XQueue[A]): Cord = {
      Cord("[", Cord.mkCord(",", f.map(Show[A].show): _*), "]")
    }
  }

  implicit def queueOrder[A: Order]: Order[XQueue[A]] = new Order[XQueue[A]] {
    import Ordering._
    @tailrec
    override def order(x: XQueue[A], y: XQueue[A]): Ordering = {
      (x.dequeueOption, y.dequeueOption) match {
        case (None, None) => EQ
        case (None, Some(_)) => LT
        case (Some(_), None) => GT
        case (Some((a, as)), Some((b, bs))) => Order[A].order(a, b) match {
          case EQ => order(as, bs)
          case o => o
        }
      }
    }
  }
}

trait QueueInstances extends QueueSubInstances {
  override type XQueue[A] = Queue[A]
  override protected[this] def factory: SeqFactory[XQueue] = Queue
  override implicit def canBuildFrom[A]: CanBuildFrom[XQueue[_], A, XQueue[A]] = Queue.canBuildFrom
}

object queue extends QueueInstances
