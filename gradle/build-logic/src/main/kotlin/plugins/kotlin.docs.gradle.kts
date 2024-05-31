package plugins

import common.*
import java.net.URI
import kotlinx.validation.ApiValidationExtension
import org.hildan.github.changelog.plugin.GitHubChangelogExtension
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

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
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
      // Override the default dokka logo and styles
      // val rootPath = rootProject.rootDir.toPath()
      // customAssets = listOf(rootPath.resolve("app-logo.svg"))
      // customStyleSheets = listOf(rootPath.resolve("logo-styles.css"))
      // templatesDir = rootPath.resolve("templates")
      footerMessage = "Copyright &copy; 2024 suresh.dev"
      homepageLink = githubRepo
      separateInheritedMembers = false
      mergeImplicitExpectActualDeclarations = false
    }
  }

  // Combined test reports
  val allTestReports by
      tasks.registering(TestReport::class) {
        destinationDirectory = layout.buildDirectory.dir("reports/tests/test")
        allprojects.forEach {
          testResults.from(it.tasks.withType<Test>(), it.tasks.withType<KotlinTest>())
        }

        doLast {
          logger.lifecycle("All test reports are aggregated in ${destinationDirectory.get()}")
        }
      }
}

// Configure if the plugin is applied to the project.
pluginManager.withPlugin("org.jetbrains.kotlinx.binary-compatibility-validator") {
  extensions.configure<ApiValidationExtension>("apiValidation") {
    validationDisabled = true
    klib { enabled = false }
  }
}

pluginManager.withPlugin("org.hildan.github.changelog") {
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

        samples.from("src/test/kotlin")

        perPackageOption {
          matchingRegex = ".*internal.*"
          suppress = true
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
      footerMessage = "Copyright &copy; 2024 suresh.dev"
      homepageLink = githubRepo
    }
  }
}
