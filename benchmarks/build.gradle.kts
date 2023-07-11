import common.mppTargetName

plugins {
  plugins.kotlin.mpp
  alias(libs.plugins.kotlinx.benchmark)
  alias(libs.plugins.kotlin.allopen)
}

allOpen { annotation("org.openjdk.jmh.annotations.State") }

benchmark {
  targets {
    register("jvm")
    register("desktop")
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
