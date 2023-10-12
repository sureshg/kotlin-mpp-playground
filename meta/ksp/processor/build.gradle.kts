plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

kotlin.sourceSets {
  commonMain { dependencies { implementation(projects.common) } }

  jvmMain {
    dependencies {
      implementation(libs.kotlin.ksp.api)
      // implementation("com.squareup:kotlinpoet-ksp")
    }

    kotlin.srcDir("src/main/kotlin")
    resources.srcDir("src/main/resources")
  }
}
