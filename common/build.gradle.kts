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
  applicationDefaultJvmArgs += jvmArguments(forAppRun = true)
}

buildConfig {
  version = project.version.toString()
  catalogVersions = project.versionCatalogMapOf()
}

dependencies {
  commonMainApi(libs.uri.kmp)
  commonMainApi(libs.ajalt.colormath)
  commonMainApi(libs.benasher44.uuid)
  commonMainApi(libs.intellij.markdown)
  commonMainApi(libs.kotlin.codepoints.deluxe)
  commonMainApi(libs.multiplatform.settings.core)
  commonMainApi(libs.parsus)
  // jvmMainApi(libs.logback.classic)
}

// Another way to add dependencies to commonMain
kotlin.sourceSets.commonMain {
  // println(implementationConfigurationName)
  dependencies {
    // implementation(libs.kotlinx.io.core)
  }
}
