package scalacture.experimental.util

import java.nio.ByteBuffer

private[scalacture] object Bytes {
  private[this] val ShortByteSize = java.lang.Short.SIZE / java.lang.Byte.SIZE
  private[this] val IntByteSize = Integer.SIZE / java.lang.Byte.SIZE
  private[this] val LongByteSize = java.lang.Long.SIZE / java.lang.Byte.SIZE
  private[this] val FloatByteSize = java.lang.Float.SIZE / java.lang.Byte.SIZE
  private[this] val DoubleByteSize = java.lang.Double.SIZE / java.lang.Byte.SIZE
  private[this] val CharByteSize = Character.SIZE / java.lang.Byte.SIZE

  def fromByte(x: Byte): Array[Byte] = Array(x)

  def fromShort(x: Short): Array[Byte] = ByteBuffer.allocate(ShortByteSize).putShort(x).array()

  def fromInt(x: Int): Array[Byte] = ByteBuffer.allocate(IntByteSize).putInt(x).array()

  def fromLong(x: Long): Array[Byte] = ByteBuffer.allocate(LongByteSize).putLong(x).array()

  def fromFloat(x: Float): Array[Byte] = ByteBuffer.allocate(FloatByteSize).putFloat(x).array()

  def fromDouble(x: Double): Array[Byte] = ByteBuffer.allocate(DoubleByteSize).putDouble(x).array()

  def fromChar(x: Char): Array[Byte] = ByteBuffer.allocate(CharByteSize).putChar(x).array()
}
