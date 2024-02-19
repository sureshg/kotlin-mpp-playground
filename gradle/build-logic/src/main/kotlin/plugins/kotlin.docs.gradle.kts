package plugins

import common.*
import java.net.URI
import kotlinx.validation.ApiValidationExtension
import org.gradle.kotlin.dsl.*
import org.hildan.github.changelog.plugin.GitHubChangelogExtension
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  org.jetbrains.dokka
  org.jetbrains.kotlinx.kover
  `test-report-aggregation`
}

// The following plugins and config apply only to a root project.
if (isRootProject) {
  apply(plugin = "org.hildan.github.changelog")
  apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")

  // Combined test & coverage report
  dependencies {
    project.subprojects
        .filter { !it.path.contains(":dep-mgmt") }
        .forEach {
          kover(it)
          testReportAggregation(it)
        }
  }

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
    dokkaSourceSets {
      // register("customSourceSet") {}
      configureEach {
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
        //  externalDocumentationLink(url = "https://kotlinlang.org/api/kotlinx-datetime/",
        // packageListUrl =
        // "https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list")
        //  externalDocumentationLink(url = "https://api.ktor.io/")
      }
    }

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
      // Dokka's stylesheets and assets with conflicting names will be overridden.
      // val rootPath = rootProject.rootDir.toPath()
      // val logoCss = rootPath.resolve("docs/css/logo-styles.css").toString()
      // val logSvg = rootPath.resolve("docs/img/app-logo.svg").toString()
      customStyleSheets = listOf(file("logo-styles.css"))
      customAssets = listOf(file("app-logo.svg"))
      footerMessage = "Copyright &copy; 2024 suresh.dev"
    }
  }
}
