plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

kotlin.sourceSets {
  commonMain { dependencies { implementation(projects.shared) } }

  jvmMain {
    dependencies {}

    kotlin.srcDir("src/main/kotlin")
    resources.srcDir("src/main/resources")
  }

  jvmTest {
    dependencies {}

    kotlin.srcDir("src/test/kotlin")
    resources.srcDir("src/test/resources")
  }
}
