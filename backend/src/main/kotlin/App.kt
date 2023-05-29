import dev.suresh.Greeting
import dev.suresh.platform

fun main() {
  println("$platform: ${Greeting().greeting()}")
  ClassLoader.getSystemClassLoader().apply {
    println(getResource("common-main-res.txt")?.readText())
    println(getResource("common-jvm-res.txt")?.readText())
    println(getResource("backend-jvm-res.txt")?.readText())
  }
}
