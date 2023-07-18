import dev.suresh.Greeting
import dev.suresh.Lang
import dev.suresh.platform
import java.util.concurrent.StructuredTaskScope

fun main() {
  println(Greeting().greeting())
  listOf("main", "jvm", "js").forEach {
    println("common-$it --> ${ClassLoader.getSystemResource("common-$it-res.txt")?.readText()}")
  }

  listOf("main", "jvm", "js").forEach {
    println("backend-$it -->${ClassLoader.getSystemResource("backend-$it-res.txt")?.readText()}")
  }

  StructuredTaskScope.ShutdownOnFailure().use {
    val task = it.fork { "Virtual thread on ${Lang("Kotlin")} $platform !" }
    it.join().throwIfFailed()
    println(task.get())
  }

  langFeatures()
  stdlibFeatures()
}
