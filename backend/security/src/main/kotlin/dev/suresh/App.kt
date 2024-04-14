package dev.suresh

fun main() {
  println("JVM Certs & Security!")
  val trustStores = TrustStore.allTrustStores()
  trustStores.forEach { println(it) }
}
