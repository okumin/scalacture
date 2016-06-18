package scalacture.experimental.util

import java.nio.charset.StandardCharsets
import scala.util.hashing.MurmurHash3

trait Hashing[A] {
  def hash(x: A): Int = hash(x, Hashing.DefaultSeed)

  def hash(x: A, seed: Int): Int
}

object Hashing {
  private  val DefaultSeed = 0x3c074a61

  def hash[A](x: A)(implicit hashing: Hashing[A]): Int = hashing.hash(x)

  def hash[A](x: A, seed: Int)(implicit hashing: Hashing[A]): Int = hashing.hash(x, seed)

  def apply[A](f: A => Array[Byte]): Hashing[A] = new Hashing[A] {
    override def hash(x: A, seed: Int): Int = MurmurHash3.bytesHash(f(x), seed)
  }

  implicit val BytesHashing: Hashing[Array[Byte]] = apply(identity)

  implicit val ByteHashing: Hashing[Byte] = apply(Bytes.fromByte)

  implicit val ShortHashing: Hashing[Short] = apply(Bytes.fromShort)

  implicit val IntHashing: Hashing[Int] = apply(Bytes.fromInt)

  implicit val LongHashing: Hashing[Long] = apply(Bytes.fromLong)

  implicit val FloatHashing: Hashing[Float] = apply(Bytes.fromFloat)

  implicit val DoubleHashing: Hashing[Double] = apply(Bytes.fromDouble)

  implicit val CharHashing: Hashing[Char] = apply(Bytes.fromChar)

  implicit val StringHashing: Hashing[String] = apply(_.getBytes(StandardCharsets.UTF_8))
}
