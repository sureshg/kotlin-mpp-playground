import com.diffplug.spotless.kotlin.KtfmtStep
import common.*
import java.time.Year
import org.hildan.github.changelog.plugin.GitHubChangelogExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
  org.jetbrains.dokka
  com.diffplug.spotless
  // org.jetbrains.`dokka-javadoc`
  // `test-report-aggregation`
}

// The following plugins and config apply only to a root project.
if (isRootProject) {
  apply(plugin = "org.hildan.github.changelog")

  // Combined test reports
  val allTestReports by
      tasks.registering(TestReport::class) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Generates aggregated test report for all tests."
        destinationDirectory = layout.buildDirectory.dir("reports/allTests")

        // Include the results from the `test` task in all subprojects
        allprojects.forEach { testResults.from(it.tasks.withType<AbstractTestTask>()) }

        doLast {
          logger.lifecycle("All test reports are aggregated in ${destinationDirectory.get()}")
        }
      }
}

dokka {
  moduleName = project.name
  dokkaSourceSets.configureEach {
    // includes.from("README.md")
    jdkVersion = kotlinJvmTarget.map { it.target.toInt() }
    enableKotlinStdLibDocumentationLink = true
    enableJdkDocumentationLink = true
    reportUndocumented = false
    skipDeprecated = true

    sourceLink {
      localDirectory = rootDir
      remoteUrl = uri("${githubRepo}/tree/main")
      remoteLineSuffix = "#L"
    }

    perPackageOption {
      matchingRegex = ".*internal.*"
      suppress = true
    }

    documentedVisibilities = setOf(VisibilityModifier.Public)

    samples.from("src/test/kotlin")

    externalDocumentationLinks {
      register("kotlinx.coroutines") { url("https://kotlinlang.org/api/kotlinx.coroutines/") }
      register("kotlinx.serialization") { url("https://kotlinlang.org/api/kotlinx.serialization/") }
      register("kotlinx-datetime") {
        url("https://kotlinlang.org/api/kotlinx-datetime/")
        packageListUrl("https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list")
      }
      register("ktor") { url("https://api.ktor.io/") }
    }

    // dokkaPublicationDirectory = rootProject.layout.buildDirectory.dir("dokkaDir")
  }

  pluginsConfiguration.html {
    footerMessage = "Copyright &copy; ${Year.now()} suresh.dev"
    homepageLink = githubRepo
    separateInheritedMembers = false
    mergeImplicitExpectActualDeclarations = false

    // customAssets.from(rootDir.resolve("app-logo.svg"))
    // customStyleSheets.from(rootDir.resolve("logo-styles.css"))
    // templatesDir = rootDir.resolve("dokka-templates")
  }
}

spotless {
  java {
    // googleJavaFormat(libs.versions.google.javaformat.get())
    palantirJavaFormat(libs.versions.palantir.javaformat.get()).formatJavadoc(true)
    target("**/*.java_disabled")
    targetExclude("**/build/**")
  }
  // if(plugins.hasPlugin(JavaPlugin::class.java)){ }

  val ktfmtVersion = maxOf(KtfmtStep.defaultVersion(), libs.versions.ktfmt.get())
  kotlin {
    ktfmt(ktfmtVersion)
    target("src/**/*.kts", "src/**/*.kt")
    trimTrailingWhitespace()
    // licenseHeader(rootProject.file("gradle/license-header.txt"))
  }

  kotlinGradle {
    ktfmt(ktfmtVersion)
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    trimTrailingWhitespace()
  }

  format("misc") {
    target("**/*.md", "**/.kte")
    targetExclude("**/build/**")
    trimTrailingWhitespace()
    indentWithSpaces(2)
  }
}

pluginManager.withPlugin("org.hildan.github.changelog") {
  configure<GitHubChangelogExtension> { githubUser = project.githubUser }
}
