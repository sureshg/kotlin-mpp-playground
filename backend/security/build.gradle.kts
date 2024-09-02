plugins {
  plugins.kotlin.mpp
  plugins.publishing
  `binary-compatibility-validator`
}

description = "Certificate and Security!"

kotlin.sourceSets {
  commonMain { dependencies { implementation(projects.shared) } }

  jvmMain {
    kotlin.srcDir("src/main/kotlin")
    resources.srcDir("src/main/resources")
  }

  jvmTest {
    kotlin.srcDir("src/test/kotlin")
    resources.srcDir("src/test/resources")
  }
}
