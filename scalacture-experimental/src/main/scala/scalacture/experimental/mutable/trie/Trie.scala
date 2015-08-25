package scalacture.experimental.mutable.trie

import scala.annotation.tailrec

class Trie[A] {
  private var value: Option[A] = None
  private[this] var children: Map[Char, Trie[A]] = Map.empty

  private def get(key: List[Char]): Option[A] = key match {
    case Nil => value
    case h :: t => children.get(h).flatMap(_.get(t))
  }

  def get(key: String): Option[A] = get(key.toList)

  @tailrec
  private def put(key: List[Char], v: A): Unit = key match {
    case Nil => value = Some(v)
    case h :: t => children.get(h) match {
      case Some(trie) => trie.put(t, v)
      case None =>
        val trie = Trie.empty[A]
        children += h -> trie
        trie.put(t, v)
    }
  }

  def put(key: String, v: A): Unit = put(key.toList, v)
}

object Trie {
  def empty[A]: Trie[A] = new Trie[A]
}
