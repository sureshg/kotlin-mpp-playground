import common.jvmTarget
import org.jetbrains.kotlin.gradle.*

plugins {
  id("dev.suresh.plugin.kotlin.mpp")
  id("dev.suresh.plugin.publishing")
}

description = "Kotlin Data Science!"

kotlin {
  jvmTarget(project)

  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.pty4j)
        // implementation(libs.graal.polyglot)
        // implementation(libs.graal.wasm)
        // implementation(fileTree("lib") { include("*.jar") })
      }

      kotlin.srcDir("src/main/kotlin")
      resources.srcDir("src/main/resources")
    }
  }

  @OptIn(ExperimentalKotlinGradlePluginApi::class) dependencies { implementation(projects.shared) }
}
