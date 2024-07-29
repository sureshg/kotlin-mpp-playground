package dev.suresh.lang

import dev.suresh.*
import io.github.oshai.kotlinlogging.KLogger
import java.util.concurrent.StructuredTaskScope
import java.util.stream.Gatherers
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import langFeatures
import stdlibFeatures

object VThread {

  context(KLogger)
  suspend fun virtualThreads() = runOnVirtualThread {
    info { (Greeting().greeting()) }
    listOf("main", "jvm", "js", "wasm").forEach {
      info { "Common-$it  --> ${ClassLoader.getSystemResource("common-$it-res.txt")?.readText()}" }
      info { "Backend-$it --> ${ClassLoader.getSystemResource("backend-$it-res.txt")?.readText()}" }
    }

    structuredConcurrency()
    langFeatures()
    stdlibFeatures()
    kotlinxMetaData()
    classFileApi()
    info { "Concurrent Gatherers: ${gatherers().size}" }
  }

  context(KLogger)
  private fun structuredConcurrency() {
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
                        Thread.sleep(100)
                      }
                      "Task $it"
                    }
                  }
                }
              }
          runCatching { sts.joinUntil(start.plus(1.seconds).toJavaInstant()) }
          tasks
        }

    info { "Total Tasks: ${taskList.size}" }
    val states = taskList.groupBy { it.state() }
    states.forEach { (t, u) -> info { "$t --> ${u.size}" } }
    check(states[StructuredTaskScope.Subtask.State.SUCCESS]?.size == 40)
    check(states[StructuredTaskScope.Subtask.State.FAILED]?.size == 20)
    check(states[StructuredTaskScope.Subtask.State.UNAVAILABLE]?.size == 40)

    StructuredTaskScope.ShutdownOnFailure().use {
      val task = it.fork { "Virtual thread on ${Lang("Kotlin")} ${platform.name}!" }
      it.join().throwIfFailed()
      info { task.get() }
    }
  }

  private fun gatherers(): List<String> {
    val mapGatherer = Gatherers.mapConcurrent<Int, String>(10) { "$it-concurrent" }
    return (1..100).toList().stream().gather(mapGatherer).toList()
  }

  context(KLogger)
  private fun kotlinxMetaData() {
    val metadataAnnotation = LocalDateTime::class.java.getAnnotation(Metadata::class.java)
    when (val metadata = KotlinClassMetadata.readLenient(metadataAnnotation)) {
      is KotlinClassMetadata.Class -> {
        val klass = metadata.kmClass
        info { klass.functions.map { it.name } }
        info { klass.properties.map { it.name } }
      }
      is KotlinClassMetadata.Unknown -> log.info { "Unknown" }
      else -> info { "Other" }
    }
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
