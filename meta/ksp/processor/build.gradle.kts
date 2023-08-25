plugins {
  plugins.kotlin.mpp
  plugins.publishing
}

kotlin.sourceSets.jvmMain {
  dependencies {
    implementation(libs.kotlin.ksp.api)
    // implementation("com.squareup:kotlinpoet-ksp")
  }

  kotlin.srcDir("src/main/kotlin")
  resources.srcDir("src/main/resources")
}
