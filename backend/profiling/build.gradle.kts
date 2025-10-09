import common.jvmTarget
import org.jetbrains.kotlin.gradle.*

plugins {
  id("dev.suresh.plugin.kotlin.mpp")
  id("dev.suresh.plugin.publishing")
}

description = "JVM Profiling and Monitoring!"

kotlin {
  jvmTarget(project)

  sourceSets {
    jvmMain {
      dependencies {
        implementation(libs.jmc.common)
        implementation(libs.jmc.jfr)
        implementation(libs.ap.jfr.converter)
        implementation(libs.bytesize)
        // implementation(libs.ap.loader.all)
      }
      kotlin.srcDir("src/main/kotlin")
      resources.srcDir("src/main/resources")
    }
  }

  @OptIn(ExperimentalKotlinGradlePluginApi::class) dependencies { implementation(projects.shared) }
}
