plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Kotlin KSP Processor"

kotlin.sourceSets {
  commonMain { dependencies { implementation(projects.shared) } }

  jvmMain {
    dependencies {
      implementation(libs.kotlin.ksp.api)
      // implementation("com.squareup:kotlinpoet-ksp")
    }

    kotlin.srcDir("src/main/kotlin")
    resources.srcDir("src/main/resources")
  }
}
