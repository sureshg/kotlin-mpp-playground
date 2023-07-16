package plugins

import common.libs
import kotlinx.benchmark.gradle.BenchmarkTarget
import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import kotlinx.benchmark.gradle.benchmark
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * It's not allowed to access `libs` from pre-compiled script plugins. The `plugins {}` block in the
 * build-logic script is executed before the version catalogs are resolved, and as a result, you
 * won't have access to the version catalog libraries directly within that block.
 *
 * [For more details](https://github.com/gradle/gradle/issues/15383#issuecomment-900629378)
 */
plugins {
  id("plugins.kotlin.mpp")
  id("org.jetbrains.kotlinx.benchmark")
  id("org.jetbrains.kotlin.plugin.allopen")
}

description = "Kotlin benchmarking tests"

allOpen { annotation("org.openjdk.jmh.annotations.State") }

benchmark {
  targets {
    register("jvm") { configureJmh() }
    register("desktop") { configureJmh() }
    // register("js")
  }

  configurations {
    named("main") {
      warmups = 5 // number of warmup iterations
      iterations = 5 // number of iterations
      iterationTime = 3 // time in seconds per iteration
      iterationTimeUnit = "ms"
      advanced("jvmForks", 3)
      advanced("jsUseBridge", true)
    }
  }
}

val kotlin = the<KotlinMultiplatformExtension>()

kotlin.sourceSets.named("commonMain") {
  dependencies { implementation(libs.kotlinx.bench.runtime) }
}

fun BenchmarkTarget.configureJmh() {
  this as JvmBenchmarkTarget
  jmhVersion = libs.versions.jmh.get()
}
