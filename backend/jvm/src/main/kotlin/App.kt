import dev.suresh.*
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.ValueLayout
import java.util.concurrent.StructuredTaskScope
import kotlinx.datetime.LocalDateTime
import kotlinx.metadata.jvm.KotlinClassMetadata

fun main() {
  println(Greeting().greeting())
  listOf("main", "jvm", "js").forEach {
    println("common-$it --> ${ClassLoader.getSystemResource("common-$it-res.txt")?.readText()}")
  }

  listOf("main", "jvm", "js").forEach {
    println("backend-$it -->${ClassLoader.getSystemResource("backend-$it-res.txt")?.readText()}")
  }

  StructuredTaskScope.ShutdownOnFailure().use {
    val task = it.fork { "Virtual thread on ${Lang("Kotlin")} ${platform().name} !" }
    it.join().throwIfFailed()
    println(task.get())
  }

  langFeatures()
  stdlibFeatures()

  getPid()
  kotlinxMetaData()
  classFileApi()
}

fun getPid() {
  val getpidAddr = SYMBOL_LOOKUP.findOrNull("getpid")
  val getpidDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT)
  val getpid = LINKER.downcallHandle(getpidAddr, getpidDesc)
  val pid = getpid.invokeExact() as Int
  assert(pid.toLong() == ProcessHandle.current().pid())
  println("getpid() = $pid")
}

fun kotlinxMetaData() {
  val metadataAnnotation = LocalDateTime::class.java.getAnnotation(Metadata::class.java)
  when (val metadata = KotlinClassMetadata.read(metadataAnnotation)) {
    is KotlinClassMetadata.Class -> {
      val klass = metadata.kmClass
      println(klass.functions.map { it.name })
      println(klass.properties.map { it.name })
    }
    is KotlinClassMetadata.Unknown -> println("Unknown")
    else -> println("Other")
  }
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
