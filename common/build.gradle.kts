import common.addModules

plugins {
  application
  plugins.kotlin.mpp
}

description = "Shared common module for all projects"

application {
  mainClass = "dev.suresh.ApplicationKt"
  applicationDefaultJvmArgs +=
      listOf(
          "--show-version",
          "--enable-preview",
          "--add-modules=$addModules",
          "--enable-native-access=ALL-UNNAMED",
      )
}

dependencies {
  // Common multiplatform dependencies
  commonMainImplementation(libs.kotlinx.io.bytestring)
  commonMainImplementation(libs.kotlinx.io.core)
  commonMainImplementation(libs.ktor.client.core)
  commonMainImplementation(libs.ktor.client.logging)
  commonMainImplementation(libs.ktor.client.serialization)
  commonMainImplementation(libs.ajalt.colormath)
  commonMainImplementation(libs.benasher44.uuid)
  commonMainImplementation(libs.intellij.markdown)
  commonMainImplementation(libs.kotlin.codepoints.deluxe)
  commonMainImplementation(libs.multiplatform.settings)
  // commonMainImplementation(libs.kotlinx.html)
  // commonMainImplementation(libs.store5)

  // jvmMainImplementation(libs.slf4j.api)
}

// kotlin.sourceSets.commonMain {
//    dependencies {
//        implementation(libs.kotlinx.io.core)
//    }
// }
