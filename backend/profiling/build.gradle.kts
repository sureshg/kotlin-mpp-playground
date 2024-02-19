plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "JVM Profiling and Monitoring!"

kotlin.sourceSets {
  commonMain { dependencies { implementation(projects.shared) } }

  jvmMain {
    dependencies {
      implementation(libs.jmc.common)
      implementation(libs.jmc.jfr)
      implementation(libs.ap.converter)
      // implementation(libs.ap.loader.all)
    }
    kotlin.srcDir("src/main/kotlin")
    resources.srcDir("src/main/resources")
  }

  jvmTest {
    kotlin.srcDir("src/test/kotlin")
    resources.srcDir("src/test/resources")
  }
}
