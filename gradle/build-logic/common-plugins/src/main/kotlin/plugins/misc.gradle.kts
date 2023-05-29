package plugins

import dev.suresh.gradle.libs
import org.gradle.kotlin.dsl.*

plugins {
  java
  wrapper
  // id("common")
  // id("gg.jte.gradle")
  id("com.diffplug.spotless")
  id("com.github.ben-manes.versions")
}

val ktfmtVersion = libs.versions.ktfmt.get()
val gjfVersion = libs.versions.google.javaformat.get()

// jte {
//   contentType = ContentType.Plain
//   generateNativeImageResources = true
//   generate()
// }

// Formatting
spotless {
  java {
    googleJavaFormat(gjfVersion)
    target("**/*.java.fix")
    targetExclude("**/build/**", "**/.gradle/**")
  }
  // if(plugins.hasPlugin(JavaPlugin::class.java)){ }

  kotlin {
    ktfmt(ktfmtVersion)
    target("**/*.kt")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**")
    // licenseHeader(rootProject.file("gradle/license-header.txt"))
  }

  kotlinGradle {
    ktfmt(ktfmtVersion)
    target("**/*.gradle.kts")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/build/**")
  }

  format("misc") {
    target("**/*.md", "**/.gitignore")
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
}

tasks {
  // Dependency version updates
  dependencyUpdates {
    checkConstraints = true

    // Run "dependencyUpdates" on included projects with a top level build file.
    gradle.includedBuilds.forEach { incBuild ->
      incBuild.projectDir
          .resolve("build.gradle.kts")
          .takeIf { it.exists() }
          ?.let { dependsOn(incBuild.task(":dependencyUpdates")) }
    }
  }

  // Reproducible builds
  withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }

  wrapper {
    gradleVersion = libs.versions.gradle.asProvider().get()
    distributionType = Wrapper.DistributionType.ALL
  }

  defaultTasks("clean", "tasks", "--all")
}
