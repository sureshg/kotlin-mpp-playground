package dev.suresh.lang

import dev.suresh.*
import io.github.oshai.kotlinlogging.KLogger
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.ValueLayout
import java.util.concurrent.StructuredTaskScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import kotlinx.metadata.jvm.KotlinClassMetadata
import langFeatures
import stdlibFeatures

object VThread {

  context(KLogger)
  fun virtualThreads() {
    info { (Greeting().greeting()) }
    listOf("main", "jvm", "js").forEach {
      info { "common-$it --> ${ClassLoader.getSystemResource("common-$it-res.txt")?.readText()}" }
    }

    listOf("main", "jvm", "js").forEach {
      info { "backend-$it -->${ClassLoader.getSystemResource("backend-$it-res.txt")?.readText()}" }
    }

    structuredConcurrency()
    langFeatures()
    stdlibFeatures()

    getPid()
    kotlinxMetaData()
    classFileApi()
  }
}

context(KLogger)
fun structuredConcurrency() {

  info { "Structured concurrency..." }
  val taskList =
      StructuredTaskScope<String>().use { sts ->
        val start = Clock.System.now()
        val tasks =
            (1..100).map {
              sts.fork {
                when (it) {
                  in 1..40 -> "Task $it"
                  in 41..60 -> kotlin.error("Error in task $it")
                  else -> {
                    while (!Thread.currentThread().isInterrupted) {
                      debug { "Task $it ..." }
                    }
                    "Task $it"
                  }
                }
              }
            }
        runCatching { sts.joinUntil(start.plus(2.seconds).toJavaInstant()) }
        tasks
      }

  info { "Total Tasks: ${taskList.size}" }
  val states = taskList.groupBy { it.state() }
  states.forEach { (t, u) -> info { "$t --> ${u.size}" } }
  check(states[StructuredTaskScope.Subtask.State.SUCCESS]?.size == 40)
  check(states[StructuredTaskScope.Subtask.State.FAILED]?.size == 20)
  check(states[StructuredTaskScope.Subtask.State.UNAVAILABLE]?.size == 40)

  StructuredTaskScope.ShutdownOnFailure().use {
    val task = it.fork { "Virtual thread on ${Lang("Kotlin")} ${platform.name} !" }
    it.join().throwIfFailed()
    info { task.get() }
  }
}

context(KLogger)
fun getPid() {
  val getpidAddr = SYMBOL_LOOKUP.findOrNull("getpid")
  val getpidDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT)
  val getpid = LINKER.downcallHandle(getpidAddr, getpidDesc)
  val pid = getpid.invokeExact() as Int
  assert(pid.toLong() == ProcessHandle.current().pid())
  info { "getpid() = $pid" }
}

context(KLogger)
fun kotlinxMetaData() {
  val metadataAnnotation = LocalDateTime::class.java.getAnnotation(Metadata::class.java)
  when (val metadata = KotlinClassMetadata.read(metadataAnnotation)) {
    is KotlinClassMetadata.Class -> {
      val klass = metadata.kmClass
      info { klass.functions.map { it.name } }
      info { klass.properties.map { it.name } }
    }
    is KotlinClassMetadata.Unknown -> log.info { "Unknown" }
    else -> info { "Other" }
  }
}

fun classFileApi() {
  //  val codeModel =
  //      Classfile.of()
  //          .parse(Class.forName("AppKt").toBytes())
  //          .methods()
  //          .filter { it.methodName().equalsString("main") }
  //          .firstNotNullOfOrNull { it.code().getOrNull() }
  //  codeModel?.elementList()?.forEach { log.info {it.toString()} }
}
