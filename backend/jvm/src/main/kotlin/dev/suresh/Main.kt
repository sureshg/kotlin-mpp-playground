package dev.suresh

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import kotlinx.metadata.jvm.KotlinClassMetadata
import langFeatures
import log
import stdlibFeatures
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.ValueLayout
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.StructuredTaskScope.Subtask
import kotlin.time.Duration.Companion.seconds

fun main() {
  log.info { (Greeting().greeting()) }
  listOf("main", "jvm", "js").forEach {
    log.info { "common-$it --> ${ClassLoader.getSystemResource("common-$it-res.txt")?.readText()}" }
  }

  listOf("main", "jvm", "js").forEach {
    log.info {
      "backend-$it -->${ClassLoader.getSystemResource("backend-$it-res.txt")?.readText()}"
    }
  }

  virtualThreads()
  langFeatures()
  stdlibFeatures()

  getPid()
  kotlinxMetaData()
  classFileApi()
}

fun virtualThreads() {
  val taskList =
      StructuredTaskScope<String>().use { sts ->
        val start = Clock.System.now()
        val tasks =
            (1..100).map {
              sts.fork {
                when (it) {
                  in 1..40 -> "Task $it"
                  in 41..60 -> error("Error in task $it")
                  else -> {
                    while (!Thread.currentThread().isInterrupted) {
                      log.debug { "Task $it ..." }
                    }
                    "Task $it"
                  }
                }
              }
            }
        runCatching { sts.joinUntil(start.plus(2.seconds).toJavaInstant()) }
        tasks
      }

  log.info { "Total Tasks: ${taskList.size}" }
  val states = taskList.groupBy { it.state() }
  states.forEach { (t, u) -> log.info { "$t --> ${u.size}" } }
  check(states[Subtask.State.SUCCESS]?.size == 40)
  check(states[Subtask.State.FAILED]?.size == 20)
  check(states[Subtask.State.UNAVAILABLE]?.size == 40)

  StructuredTaskScope.ShutdownOnFailure().use {
    val task = it.fork { "Virtual thread on ${Lang("Kotlin")} ${platform.name} !" }
    it.join().throwIfFailed()
    log.info { task.get() }
  }
}

fun getPid() {
  val getpidAddr = SYMBOL_LOOKUP.findOrNull("getpid")
  val getpidDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT)
  val getpid = LINKER.downcallHandle(getpidAddr, getpidDesc)
  val pid = getpid.invokeExact() as Int
  assert(pid.toLong() == ProcessHandle.current().pid())
  log.info { "getpid() = $pid" }
}

fun kotlinxMetaData() {
  val metadataAnnotation = LocalDateTime::class.java.getAnnotation(Metadata::class.java)
  when (val metadata = KotlinClassMetadata.read(metadataAnnotation)) {
    is KotlinClassMetadata.Class -> {
      val klass = metadata.kmClass
      log.info { klass.functions.map { it.name } }
      log.info { klass.properties.map { it.name } }
    }
    is KotlinClassMetadata.Unknown -> log.info { "Unknown" }
    else -> log.info { "Other" }
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
