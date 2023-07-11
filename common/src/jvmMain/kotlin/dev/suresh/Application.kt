package dev.suresh

import java.util.concurrent.StructuredTaskScope

/** Run with **--enable-preview --add-modules=ALL-SYSTEM** jvm arguments */
fun main() {
  println(Greeting().greeting())
  ClassLoader.getSystemClassLoader().apply {
    println(getResource("common-main-res.txt")?.readText())
    println(getResource("common-jvm-res.txt")?.readText())
  }
  StructuredTaskScope.ShutdownOnFailure().use {
    val subTask = it.fork { "Virtual thread on ${Lang("Kotlin")} $platform !" }
    it.join().throwIfFailed()
    println(subTask.get())
  }

  langFeatures()
  stdlibFeatures()
}
