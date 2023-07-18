package plugins

import common.kotlinJvmTarget
import common.libs
import java.net.URI
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  id("org.jetbrains.dokka")
  id("org.jetbrains.kotlinx.kover")
}

tasks {
  withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
      moduleName = project.name
      jdkVersion = kotlinJvmTarget.map { it.target.toInt() }
      noStdlibLink = false
      noJdkLink = false
      reportUndocumented = false
      skipDeprecated = true
      includes.from("README.md")

      pluginsMapConfiguration =
          mapOf(
              "org.jetbrains.dokka.base.DokkaBase" to
                  """{
                 "footerMessage": "Copyright &copy; 2023 Suresh"
                 }""")

      sourceLink {
        sourceLink {
          remoteUrl = libs.versions.publish.scm.url.map { URI("$it/tree/main/src").toURL() }
          remoteLineSuffix = "#L"
        }

        externalDocumentationLink {
          url = URI("https://kotlin.github.io/kotlinx.coroutines/package-list").toURL()
        }

        externalDocumentationLink {
          url = URI("https://kotlinlang.org/api/kotlinx.serialization/package-list").toURL()
        }

        perPackageOption {
          matchingRegex = "kotlin($|\\.).*"
          skipDeprecated = false
          reportUndocumented = true // Emit warnings about not documented members
          includeNonPublic = false
        }
      }

      //  val rootPath = rootProject.rootDir.toPath()
      //  val logoCss = rootPath.resolve("docs/css/logo-styles.css").toString().replace('\\', '/')
      //  val paletteSvg =
      // rootPath.resolve("docs/img/wordmark_small_dark.svg").toString().replace('\\', '/')
      //  pluginsMapConfiguration = mapOf(
      //      "org.jetbrains.dokka.base.DokkaBase" to """{
      //          "customStyleSheets": ["$logoCss"],
      //      "customAssets": ["$paletteSvg"],
      //      "footerMessage": "Copyright &copy; 2021 AJ Alt"
      //  }"""
      //  )
    }
  }
}
