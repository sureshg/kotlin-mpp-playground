import dev.suresh.*
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.ValueLayout
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
  classFileApi()
  getPid()
}

fun classFileApi() {
  //  val codeModel =
  //      Classfile.of()
  //          .parse(Class.forName("AppKt").toBytes())
  //          .methods()
  //          .filter { it.methodName().equalsString("main") }
  //          .firstNotNullOfOrNull { it.code().getOrNull() }
  //  codeModel?.elementList()?.forEach { println(it.toString()) }
}

fun getPid() {
  val getpidAddr = SYMBOL_LOOKUP.findOrNull("getpid")
  val getpidDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT)
  val getpid = LINKER.downcallHandle(getpidAddr, getpidDesc)
  val pid = getpid.invokeExact() as Int
  assert(pid.toLong() == ProcessHandle.current().pid())
  println("getpid() = $pid")
}
