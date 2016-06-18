package scalacture.experimental.mutable.bloomfilter

import scala.collection.mutable
import scalacture.experimental.util.{Hashing, Math}

/**
 * BloomFilter, a data structure that can test whether the specified element is a member or not.
 * Compared to ordinary sets, e.g. implemented by hash table, BloomFilter needs less space.
 * But it is possible that BloomFilter mistakes a non stored element for a member of this,
 * it is called false positive. On the other hands, BloomFilter never mistakes a member for a non member.
 *
 * If following n, m and k are given,
 * n = the number of members of this BloomFilter
 * m = the number of bits that this BloomFilter uses
 * k = the number of hash values
 * the false positive probability is
 * (1 - (1 - 1 / m) `^` kn) `^` k
 *
 * @param m the number of bits
 * @param k the number of hash values for each element
 * @param bitSet BitSet that stores elements
 */
class BloomFilter[A: Hashing] private (private val m: Int,
                                       private val k: Int,
                                       private var bitSet: mutable.BitSet) {

  def isAccepted(elem: A): Boolean = {
    BloomFilter.hash(elem, k).forall { h => bitSet.contains(math.abs(h % m)) }
  }

  def add(elem: A): Unit = {
    BloomFilter.hash(elem, k).foreach { h =>
      bitSet += math.abs(h % m)
    }
  }

  def |(that: BloomFilter[A]): BloomFilter[A] = {
    require(that.m == m)
    require(that.k == k)
    new BloomFilter(m, k, bitSet | that.bitSet)
  }

  def |=(that: BloomFilter[A]): BloomFilter[A] = {
    require(that.m == m)
    require(that.k == k)
    bitSet |= that.bitSet
    this
  }
}

object BloomFilter {
  private def hash[A](x: A, num: Int)(implicit hashing: Hashing[A]): Seq[Int] = {
    (1 to num).map { i => Hashing.hash(x, i) }
  }

  /**
   * Creates a new instance of BloomFilter.
   */
  def empty[A](bitSize: Int, numOfHashFunctions: Int)(implicit hashing: Hashing[A]): BloomFilter[A] = {
    require(bitSize > 0, "The number of bits must be greater than 0.")
    require(numOfHashFunctions > 0, "The number of hash functions must be greater than 0.")
    val length = ((bitSize - 1) >>> 6) + 1
    val bits = Array.fill(length)(0L)
    val bitSet = mutable.BitSet.fromBitMaskNoCopy(bits)
    new BloomFilter[A](bitSize, numOfHashFunctions, bitSet)
  }

  /**
   * Creates a new instance of BloomFilter with the false positive probability.
   * BloomFilter which this method creates satisfy the condition that
   * the expected probability does not exceed the specified allowableFalsePositiveProbability
   * as long as the num of stored element is smaller than or equal to n.
   * In addition to that, this method creates as small size BloomFilter as possible.
   * @param n the size to be stored in BloomFilter
   * @param allowableFalsePositiveProbability the upper bound of the false positive probability
   * @return BloomFilter
   */
  def withFalsePositiveRate[A: Hashing](n: Int,
                                        allowableFalsePositiveProbability: Double): BloomFilter[A] = {
    // m = the size of bit array
    // k = the number of hash functions
    // p = the expected false positive probability.
    // After n messages have been stored,
    // the probability that the specific bit is still 0 is (1 - 1 / m) ^ kn.
    // So p is (1 - (1 - 1 / m) ^ kn) ^ k.
    // Since the smallest m is given when half the bits are 1 and half are 0, following k is given.
    val k = math.ceil(Math.log2(allowableFalsePositiveProbability)).toInt
    // If half the bits are 1 and half are 0, m = - n * log2(falseNegativeProbability) * log2(e).
    val m = math.ceil(- n * math.log(allowableFalsePositiveProbability) / math.pow(math.log(2), 2)).toInt
    empty(m, k)
  }

  /**
   * Creates a new instance of BloomFilter with the specified number of bits.
   * And this method minimizes the expected false positive probability given n elements.
   * @param n the size to be stored in BloomFilter
   * @param bitSize the number of bits
   * @return BloomFilter
   */
  def withBitSize[A: Hashing](n: Int, bitSize: Int): BloomFilter[A] = {
    // the number of hash functions that minimizes the false positive probability
    val k = math.ceil(bitSize / n * Math.Ln2).toInt
    empty(bitSize, k)
  }
}
