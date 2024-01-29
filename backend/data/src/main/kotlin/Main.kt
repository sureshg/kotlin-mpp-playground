import com.ionspin.kotlin.bignum.integer.BigInteger
import dev.suresh.Greeting

fun main() {
  println("Hello Kotlin Data! ${Greeting().greeting()}")

  val b1 = BigInteger.parseString("12345678901234567890", 10)
  val b2 = BigInteger.parseString("323456789012345678901", 10)
  val s = b1 + b2

  println(b1.bitLength())
  println(b2.bitLength())
  println(s)
  println(s.bitLength())
}
