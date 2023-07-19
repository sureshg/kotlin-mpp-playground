package plugins

import common.libs
import java.net.URI
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  id("org.jetbrains.dokka")
  id("org.jetbrains.kotlinx.kover")
}

tasks {
  withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
      moduleName = project.name
      jdkVersion = 20
      // jdkVersion = kotlinJvmTarget.map { it.target.toInt() }
      noStdlibLink = false
      noJdkLink = false
      reportUndocumented = false
      skipDeprecated = true
      includes.from("README.md")

      documentedVisibilities = setOf(Visibility.PUBLIC, Visibility.PROTECTED)

      sourceLink {
        localDirectory = rootProject.projectDir
        remoteUrl = libs.versions.publish.scm.url.map { URI("$it/tree/main").toURL() }
        remoteLineSuffix = "#L"
      }

      externalDocumentationLink {
        url = URI("https://kotlin.github.io/kotlinx.coroutines/package-list").toURL()
      }

      externalDocumentationLink {
        url = URI("https://kotlinlang.org/api/kotlinx.serialization/package-list").toURL()
      }
    }

    pluginsMapConfiguration =
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to
                """{ "footerMessage": "Copyright &copy; 2023 Suresh"}""")

    //  val rootPath = rootProject.rootDir.toPath()
    //  val logoCss = rootPath.resolve("docs/css/logo-styles.css").toString().replace('\\', '/')
    //  val paletteSvg = rootPath.resolve("docs/img/img.svg").toString().replace('\\', '/')
    //  pluginsMapConfiguration = mapOf(
    //      "org.jetbrains.dokka.base.DokkaBase" to """{
    //          "customStyleSheets": ["$logoCss"],
    //      "customAssets": ["$paletteSvg"],
    //      "footerMessage": "Copyright &copy; 2021 AJ Alt"
    //     }"""
    //  )
  }
}
