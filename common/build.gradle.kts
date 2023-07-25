import common.jvmArguments
import common.versionCatalogMapOf

plugins {
  application
  plugins.kotlin.mpp
  plugins.publishing
}

description = "Shared common module for all projects"

application {
  mainClass = libs.versions.app.mainclass
  applicationDefaultJvmArgs += jvmArguments()
}

buildConfig {
  version = project.version.toString()
  catalogVersions = project.versionCatalogMapOf()
}

dependencies {
  // Common multiplatform dependencies
  commonMainImplementation(libs.kotlinx.io.core)
  commonMainImplementation(libs.ktor.client.core)
  commonMainImplementation(libs.ktor.client.content.negotiation)
  commonMainImplementation(libs.ktor.client.encoding)
  commonMainImplementation(libs.ktor.client.logging)
  commonMainImplementation(libs.ktor.client.resources)
  commonMainImplementation(libs.ktor.client.auth)
  commonMainImplementation(libs.ktor.serialization.json)
  commonMainImplementation(libs.ajalt.colormath)
  commonMainImplementation(libs.benasher44.uuid)
  commonMainImplementation(libs.intellij.markdown)
  commonMainImplementation(libs.kotlin.codepoints.deluxe)
  commonMainImplementation(libs.multiplatform.settings.core)

  jvmMainApi(libs.kotlin.retry)
  // jvmMainImplementation(libs.slf4j.api)
}

// kotlin.sourceSets.commonMain {
//    dependencies {
//        implementation(libs.kotlinx.io.core)
//    }
// }
