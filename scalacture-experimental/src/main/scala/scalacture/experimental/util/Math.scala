package scalacture.experimental.util

private[scalacture] object Math {
  val Ln2 = math.log(2.0)

  def log2(x: Double): Double = math.log(x) / Ln2
}
