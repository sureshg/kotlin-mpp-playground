import common.jvmArguments

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
  val catalog = project.versionCatalogs.named("libs")
  val catalogMap = catalog.versionAliases.associateWith { catalog.findVersion(it).get().toString() }
  catalogVersions = catalogMap
}

dependencies {
  // Common multiplatform dependencies
  commonMainImplementation(libs.kotlinx.io.core)
  commonMainImplementation(libs.ktor.client.core)
  commonMainImplementation(libs.ktor.client.logging)
  commonMainImplementation(libs.ktor.client.content.negotiation)
  commonMainImplementation(libs.ktor.serialization.json)
  commonMainImplementation(libs.ktor.client.resources)
  commonMainImplementation(libs.ajalt.colormath)
  commonMainImplementation(libs.benasher44.uuid)
  commonMainImplementation(libs.intellij.markdown)
  commonMainImplementation(libs.kotlin.codepoints.deluxe)
  commonMainImplementation(libs.multiplatform.settings.core)
  // commonMainImplementation(libs.kotlinx.html)
  // commonMainImplementation(libs.store5)

  // jvmMainImplementation(libs.slf4j.api)
}

// kotlin.sourceSets.commonMain {
//    dependencies {
//        implementation(libs.kotlinx.io.core)
//    }
// }
