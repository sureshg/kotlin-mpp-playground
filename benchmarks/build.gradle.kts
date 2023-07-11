import common.mppTargetName
import kotlinx.benchmark.gradle.BenchmarkTarget
import kotlinx.benchmark.gradle.JvmBenchmarkTarget

plugins {
  plugins.kotlin.mpp
  alias(libs.plugins.kotlinx.benchmark)
  alias(libs.plugins.kotlin.allopen)
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

mppTargetName = "jvm"

dependencies {
  commonMainImplementation(projects.common)
  commonMainImplementation(libs.kotlinx.bench.runtime)
}

fun BenchmarkTarget.configureJmh() {
  this as JvmBenchmarkTarget
  jmhVersion = libs.versions.jmh.get()
}
