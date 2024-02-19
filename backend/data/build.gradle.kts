plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Kotlin Data Science!"

kotlin.sourceSets {
  commonMain { dependencies { implementation(projects.shared) } }

  jvmMain {
    dependencies {
      implementation(libs.pty4j.get().toString()) {
        exclude(group = "org.jetbrains.pty4j", module = "purejavacomm")
      }
      implementation(fileTree("lib") { include("*.jar") })
      // implementation(libs.graal.polyglot)
      // implementation(libs.graal.wasm)
    }

    kotlin.srcDir("src/main/kotlin")
    resources.srcDir("src/main/resources")
  }

  jvmTest {
    kotlin.srcDir("src/test/kotlin")
    resources.srcDir("src/test/resources")
  }
}
