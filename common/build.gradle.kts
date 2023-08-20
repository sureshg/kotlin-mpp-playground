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
  commonMainApi(libs.kotlinx.io.core)
  commonMainApi(libs.ktor.client.core)
  commonMainApi(libs.ktor.client.content.negotiation)
  commonMainApi(libs.ktor.client.encoding)
  commonMainApi(libs.ktor.client.logging)
  commonMainApi(libs.ktor.client.resources)
  commonMainApi(libs.ktor.client.auth)
  commonMainApi(libs.ktor.serialization.json)
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.ajalt.colormath)
  commonMainApi(libs.benasher44.uuid)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.core)

  jvmMainApi(libs.kotlin.retry)
  // jvmMainImplementation(libs.slf4j.api)
}

// kotlin.sourceSets.commonMain {
//    dependencies {
//        implementation(libs.kotlinx.io.core)
//    }
// }
