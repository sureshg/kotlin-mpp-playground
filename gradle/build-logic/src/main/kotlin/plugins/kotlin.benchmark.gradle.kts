package plugins

import common.libs
import kotlinx.benchmark.gradle.BenchmarkTarget
import kotlinx.benchmark.gradle.KotlinJvmBenchmarkTarget
import kotlinx.benchmark.gradle.benchmark

/**
 * It's not allowed to access `libs` from pre-compiled script plugins. The `plugins {}` block in the
 * build-logic script is executed before the version catalogs are resolved, and as a result, you
 * won't have access to the version catalog libraries directly within that block.
 *
 * [For more details](https://github.com/gradle/gradle/issues/15383#issuecomment-900629378)
 */
plugins {
  id("plugins.kotlin.mpp")
  kotlin("plugin.allopen")
  org.jetbrains.kotlinx.benchmark
}

group = libs.versions.group.get()

description = "Kotlin benchmarking tests"

allOpen { annotation("org.openjdk.jmh.annotations.State") }

benchmark {
  targets {
    register("jvm") { configureJmh() }
    // register("desktop") { configureJmh() }
    // register("macosArm64")
    // register("macosX64")
    // register("linuxX64")
    // register("js")
    // register("wasmJs")
  }

  configurations {
    named("main") {
      warmups = 5 // number of warmup iterations
      iterations = 5 // number of iterations
      iterationTime = 3 // time in seconds per iteration
      iterationTimeUnit = "ms"
      advanced("jvmForks", "definedByJmh")
      advanced("jsUseBridge", true)
    }
  }
}

// val kotlin = the<KotlinMultiplatformExtension>()
kotlin.sourceSets.commonMain { dependencies { implementation(libs.kotlinx.bench.runtime) } }

tasks {
  // withType<JmhBytecodeGeneratorTask> { }
}

fun BenchmarkTarget.configureJmh() {
  this as KotlinJvmBenchmarkTarget
  jmhVersion = libs.versions.jmh.get()
}
