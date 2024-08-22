package plugins

import common.*
import java.net.URI
import org.hildan.github.changelog.plugin.GitHubChangelogExtension
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

plugins {
  org.jetbrains.dokka
  com.diffplug.spotless
  `test-report-aggregation`
}

// The following plugins and config apply only to a root project.
if (isRootProject) {
  apply(plugin = "org.hildan.github.changelog")

  // Combined test report
  dependencies {
    allprojects.filter { !it.path.contains(":dep-mgmt") }.forEach { testReportAggregation(it) }
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

pluginManager.withPlugin("org.hildan.github.changelog") {
  configure<GitHubChangelogExtension> { githubUser = project.githubUser }
}

spotless {
  java {
    // googleJavaFormat(libs.versions.google.javaformat.get())
    palantirJavaFormat(libs.versions.palantir.javaformat.get()).formatJavadoc(true)
    target("**/*.java_disabled")
    targetExclude("**/build/**")
  }
  // if(plugins.hasPlugin(JavaPlugin::class.java)){ }

  val ktfmtVersion = libs.versions.ktfmt.get()
  kotlin {
    ktfmt(ktfmtVersion)
    target("**/*.kt")
    targetExclude("**/build/**", "**/Service.kt")
    trimTrailingWhitespace()
    endWithNewline()
    // licenseHeader(rootProject.file("gradle/license-header.txt"))
  }

  kotlinGradle {
    ktfmt(ktfmtVersion)
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    trimTrailingWhitespace()
    endWithNewline()
  }

  format("misc") {
    target("**/*.md", "**/.gitignore", "**/.kte")
    targetExclude("**/build/**")
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
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

      samples.from("src/test/kotlin")

      perPackageOption {
        matchingRegex = ".*internal.*"
        suppress = true
      }
      // externalDocumentationLink(url = "https://kotlinlang.org/api/kotlinx.coroutines/")
      // externalDocumentationLink(url = "https://kotlinlang.org/api/kotlinx.serialization/")
      // externalDocumentationLink(url = "https://kotlinlang.org/api/kotlinx-datetime/",
      // packageListUrl =
      // "https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list")
      // externalDocumentationLink(url = "https://api.ktor.io/")
    }

    //  pluginsMapConfiguration = mapOf("org.jetbrains.dokka.base.DokkaBase" to """{ "templatesDir"
    // : "${projectDir.toString().replace('\\','/')}/../dokka-templates" }""")

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
      footerMessage = "Copyright &copy; 2024 suresh.dev"
      homepageLink = githubRepo
    }
  }
}
