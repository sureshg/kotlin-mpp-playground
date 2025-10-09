import common.jvmTarget
import org.jetbrains.kotlin.gradle.*

plugins {
  id("dev.suresh.plugin.kotlin.mpp")
  id("dev.suresh.plugin.publishing")
}

description = "Certificate and Security!"

kotlin {
  jvmTarget(project)

  sourceSets {
    jvmMain {
      kotlin.srcDir("src/main/kotlin")
      resources.srcDir("src/main/resources")
    }

    jvmTest {
      kotlin.srcDir("src/test/kotlin")
      resources.srcDir("src/test/resources")
    }
  }

  @OptIn(ExperimentalKotlinGradlePluginApi::class) dependencies { implementation(projects.shared) }
}
