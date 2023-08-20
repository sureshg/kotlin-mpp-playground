package plugins

import common.githubRepo
import common.githubUser
import common.kotlinJvmTarget
import java.net.URI
import kotlinx.validation.ApiValidationExtension
import org.gradle.kotlin.dsl.*
import org.hildan.github.changelog.plugin.GitHubChangelogExtension
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  org.jetbrains.dokka
  org.jetbrains.kotlinx.kover
  `test-report-aggregation`
}

// The following plugins and config apply only to a root project.
if (project == rootProject) {
  apply(plugin = "org.hildan.github.changelog")
  apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")

  // For combined Kotlin coverage report
  dependencies { project.subprojects.forEach { kover(it) } }

  // Dokka multi-module config.
  tasks.withType<DokkaMultiModuleTask>().configureEach {
    description = project.description.orEmpty()
    moduleName = project.name
  }
}

// Configure if the plugin is applied to the project.
plugins.withId("org.jetbrains.kotlinx.binary-compatibility-validator") {
  extensions.configure<ApiValidationExtension>("apiValidation") { validationDisabled = true }
}

plugins.withId("org.hildan.github.changelog") {
  the<GitHubChangelogExtension>().run { githubUser = project.githubUser }
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
      // includes.from("README.md")

      documentedVisibilities = setOf(Visibility.PUBLIC, Visibility.PROTECTED)

      sourceLink {
        localDirectory = rootProject.projectDir
        remoteUrl = URI("${githubRepo}/tree/main").toURL()
        remoteLineSuffix = "#L"
      }

      //  externalDocumentationLink(url = "https://kotlinlang.org/api/kotlinx.coroutines/")
      //  externalDocumentationLink(url = "https://kotlinlang.org/api/kotlinx.serialization/")
      //  externalDocumentationLink(
      //      url = "https://kotlinlang.org/api/kotlinx-datetime/",
      //      packageListUrl =
      //          "https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list",
      //  )
      //  externalDocumentationLink(url = "https://api.ktor.io/")
    }

    pluginsMapConfiguration =
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to
                """{ "footerMessage": "Copyright &copy; 2023 Dokka"}""")

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
