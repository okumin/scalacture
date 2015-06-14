package scalacture.helper

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.WordSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scala.collection.generic.{CanBuildFrom, GenericTraversableTemplate, SeqFactory}
import scala.collection.{SeqLike, breakOut}
import scala.language.higherKinds

abstract class SeqSpec[A: Arbitrary, CC[X] <: Seq[X] with SeqLike[X, CC[X]] with GenericTraversableTemplate[X, CC]](implicit private[this] val seqCBF: CanBuildFrom[CC[_], A, CC[A]])
  extends WordSpec
  with GeneratorDrivenPropertyChecks {

  protected[this] def factory: SeqFactory[CC]

  protected[this] val elemsAndIndex: Gen[(List[A], Int)] = {
    for {
      elems <- Gen.listOf(Arbitrary.arbitrary[A])
      n = elems.size
      index <- Gen.chooseNum(-1, n + 1, 0, 1, n - 1, n)
    } yield (elems, index)
  }

  "A Seq" should {
    "be able to seriazlie" in {
      forAll { elems: List[A] =>
        val seq = factory(elems: _*)
        val outputStream = new ByteArrayOutputStream()
        new ObjectOutputStream(outputStream).writeObject(seq)
        val bytes = outputStream.toByteArray

        val inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))
        val deserizized = inputStream.readObject().asInstanceOf[CC[A]]
        assert(deserizized === seq)
      }
    }

    "apply" when {
      "specifiying correct index" in {
        forAll(elemsAndIndex) {
          case (elems, index) =>
            whenever(index >= 0 && index < elems.size) {
              assert(factory(elems: _*)(index) === elems(index))
            }
        }
      }

      "index is out of range" in {
        val elemsAndIndex = for {
          elems <- Gen.listOf(Arbitrary.arbitrary[A])
          n = elems.size
          index <- Gen.oneOf(Gen.negNum[Int], Gen.choose(n, Int.MaxValue))
        } yield (elems, index)
        forAll(elemsAndIndex) {
          case (elems, index) =>
            intercept[IndexOutOfBoundsException] { factory(elems: _*)(index) }
            intercept[IndexOutOfBoundsException] { elems(index) }
        }
      }
    }

    "iterator" in {
      forAll { elems: List[A] =>
        assert(factory(elems: _*).iterator.toList === elems)
      }
    }

    "++" when {
      "same-result-type" in {
        forAll { (elems1: List[A], elems2: List[A]) =>
          val actual: CC[A] = factory(elems1: _*) ++ factory(elems2: _*)
          assert(actual === elems1 ++ elems2)
        }
      }

      "converting into super type" in {
        forAll { (elems1: List[A], elems2: List[A]) =>
          val actual: Seq[A] = factory(elems1: _*) ++ factory(elems2: _*)
          assert(actual === elems1 ++ elems2)
        }
      }

      "converting into another type" in {
        forAll { (elems1: List[A], elems2: List[A]) =>
          val actual: List[A] = factory(elems1: _*).++(factory(elems2: _*))(breakOut)
          assert(actual === elems1 ++ elems2)
        }
      }
    }

    "++:" when {
      "same-result-type" in {
        forAll { (elems1: List[A], elems2: List[A]) =>
          val actual: CC[A] = factory(elems1: _*) ++: factory(elems2: _*)
          assert(actual === elems1 ++: elems2)
        }
      }

      "converting into super type" in {
        forAll { (elems1: List[A], elems2: List[A]) =>
          val actual: Seq[A] = factory(elems1: _*).++:(factory(elems2: _*))
          assert(actual === elems1.++:(elems2))
        }
      }

      "converting into another type" in {
        forAll { (elems1: List[A], elems2: List[A]) =>
          val actual: List[A] = factory(elems1: _*).++:(factory(elems2: _*))(breakOut)
          assert(actual === elems1.++:(elems2))
        }
      }
    }

    "+:" when {
      "same-result-type" in {
        forAll { (elems: List[A], elem: A) =>
          val actual: CC[A] = elem +: factory(elems: _*)
          assert(actual === elem +: elems)
        }
      }

      "converting into super type" in {
        forAll { (elems: List[A], elem: A) =>
          val actual: Seq[A] = elem +: factory(elems: _*)
          assert(actual === elem +: elems)
        }
      }

      "converting into another type" in {
        forAll { (elems: List[A], elem: A) =>
          val actual: List[A] = factory(elems: _*).+:(elem)(breakOut)
          assert(actual === elem +: elems)
        }
      }
    }

    ":+" when {
      "same-result-type" in {
        forAll { (elems: List[A], elem: A) =>
          val actual: CC[A] = factory(elems: _*) :+ elem
          assert(actual === elems :+ elem)
        }
      }

      "converting into super type" in {
        forAll { (elems: List[A], elem: A) =>
          val actual: Seq[A] = factory(elems: _*) :+ elem
          assert(actual === elems :+ elem)
        }
      }

      "converting into another type" in {
        forAll { (elems: List[A], elem: A) =>
          val actual: List[A] = factory(elems: _*).:+(elem)(breakOut)
          assert(actual === elems :+ elem)
        }
      }
    }

    "drop" in {
      forAll { (elems: List[A], n: Int) =>
        assert(factory(elems: _*).drop(n) === elems.drop(n))
      }
    }

    "dropRight" in {
      forAll { (elems: List[A], n: Int) =>
        assert(factory(elems: _*).dropRight(n) === elems.dropRight(n))
      }
    }

    "dropWhile" in {
      forAll { (elems: List[A], elem: A) =>
        assert(factory(elems: _*).dropWhile(_ != elem) === elems.dropWhile(_ != elem))
      }
    }

    "endsWith" in {
      forAll { (elems: List[A], end: List[A]) =>
        assert(factory(elems: _*).endsWith(end) === elems.endsWith(end))
      }
    }

    "filter" in {
      forAll { (elems: List[A], elem: A) =>
        val actual: CC[A] = factory(elems: _*).filter(_ == elem)
        assert(actual === elems.filter(_ == elem))
      }
    }

    "head" when {
      "Seq is non-empty" in {
        forAll(Gen.nonEmptyListOf(Arbitrary.arbitrary[A])) { elems: List[A] =>
          assert(factory(elems: _*).head === elems.head)
        }
      }

      "Seq is empty" in {
        intercept[NoSuchElementException] { factory().head }
        intercept[NoSuchElementException] { Nil.head }
      }
    }

    "init" when {
      "Seq is non-empty" in {
        forAll(Gen.nonEmptyListOf(Arbitrary.arbitrary[A])) { elems: List[A] =>
          assert(factory(elems: _*).init === elems.init)
        }
      }

      "Seq is empty" in {
        intercept[UnsupportedOperationException] { factory().init }
        intercept[UnsupportedOperationException] { Nil.init }
      }
    }

    "last" when {
      "Seq is non-empty" in {
        forAll(Gen.nonEmptyListOf(Arbitrary.arbitrary[A])) { elems: List[A] =>
          assert(factory(elems: _*).last === elems.last)
        }
      }

      "Seq is empty" in {
        intercept[NoSuchElementException] { factory().last }
        intercept[NoSuchElementException] { Nil.last }
      }
    }

    "map" when {
      "same-result-type" in {
        forAll { elems: List[A] =>
          val actual: CC[A] = factory(elems: _*).map(identity)
          assert(actual === elems.map(identity))
        }
      }

      "converting into super type" in {
        forAll { elems: List[A] =>
          val actual: Seq[A] = factory(elems: _*).map(identity)
          assert(actual === elems.map(identity))
        }
      }

      "converting into another type" in {
        forAll { elems: List[A] =>
          val actual: List[A] = factory(elems: _*).map(identity)(breakOut)
          assert(actual === elems.map(identity))
        }
      }
    }

    "padTo" when {
      "same-result-type" in {
        forAll(elemsAndIndex, Arbitrary.arbitrary[A]) {
          case ((elems, len), elem) =>
            val actual: CC[A] = factory(elems: _*).padTo(len, elem)
            assert(actual === elems.padTo(len, elem))
        }
      }

      "converting into super type" in {
        forAll(elemsAndIndex, Arbitrary.arbitrary[A]) {
          case ((elems, len), elem) =>
            val actual: Seq[A] = factory(elems: _*).padTo(len, elem)
            assert(actual === elems.padTo(len, elem))
        }
      }

      "converting into another type" in {
        forAll(elemsAndIndex, Arbitrary.arbitrary[A]) {
          case ((elems, len), elem) =>
            val actual: List[A] = factory(elems: _*).padTo(len, elem)(breakOut)
            assert(actual === elems.padTo(len, elem))
        }
      }
    }

    "prefixLength" in {
      forAll { (elems: List[A], elem: A) =>
        assert(factory(elems: _*).prefixLength(_ != elem) === elems.prefixLength(_ != elem))
      }
    }

    "reverse" in {
      forAll { elems: List[A] =>
        assert(factory(elems: _*).reverse === elems.reverse)
      }
    }

    "size" in {
      forAll { elems: List[A] =>
        assert(factory(elems: _*).size === elems.size)
      }
    }

    "splitAt" in {
      forAll(elemsAndIndex) {
        case (elems, index) =>
          assert(factory(elems: _*).splitAt(index) === elems.splitAt(index))
      }
    }

    "tail" when {
      "Seq is non-empty" in {
        forAll(Gen.nonEmptyListOf(Arbitrary.arbitrary[A])) { elems: List[A] =>
          assert(factory(elems: _*).tail === elems.tail)
        }
      }

      "Seq is empty" in {
        intercept[UnsupportedOperationException] { factory().tail }
        intercept[UnsupportedOperationException] { Nil.tail }
      }
    }

    "takeRight" in {
      forAll(elemsAndIndex) {
        case (elems, index) =>
          assert(factory(elems: _*).takeRight(index) === elems.takeRight(index))
      }
    }
  }
}
