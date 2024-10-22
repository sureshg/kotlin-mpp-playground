import common.jvmTarget

plugins {
  dev.suresh.plugin.kotlin.mpp
  dev.suresh.plugin.publishing
}

description = "Kotlin Data Science!"

kotlin {
  jvmTarget(project)

  sourceSets {
    commonMain { dependencies { implementation(projects.shared) } }

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

    jvmTest {
      kotlin.srcDir("src/test/kotlin")
      resources.srcDir("src/test/resources")
    }
  }
}
