package scalacture.experimental.mutable.trie

import org.scalatest.WordSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scala.collection.mutable

class TrieSpec extends WordSpec with GeneratorDrivenPropertyChecks {
  "TrieMap" should {
    "behave as Map" in {
      forAll { kvs: List[(String, Int)] =>
        val map = mutable.Map.empty[String, Int]
        val trie = Trie.empty[Int]
        kvs.foreach {
          case (k, v) =>
            map.put(k, v)
            trie.put(k, v)
            assert(map.get(k) === Some(v))
            assert(trie.get(k) === Some(v))
        }
        kvs.foreach {
          case (k, _) => assert(map.get(k) === trie.get(k))
        }
      }
    }
  }
}
