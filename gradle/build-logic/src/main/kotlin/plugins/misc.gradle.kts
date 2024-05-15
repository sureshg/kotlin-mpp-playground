package plugins

import common.libs
import org.gradle.kotlin.dsl.*

plugins {
  java
  wrapper
  com.diffplug.spotless
  com.github.`ben-manes`.versions
  dev.iurysouza.modulegraph
  // id("plugins.common")
  // id("gg.jte.gradle")
}

// jte {
//   contentType = ContentType.Plain
//   generateNativeImageResources = true
//   generate()
// }

// Formatting
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

moduleGraphConfig {
  readmePath = "./README.md"
  heading = "### Module Dependency"
}

tasks {

  // Set up git hooks
  register<Copy>("setUpGitHooks") {
    group = "help"
    from("$rootDir/gradle/.githooks")
    into("$rootDir/.git/hooks")
  }

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
    dirPermissions {
      // 0b111111101
      unix("rwxrwxr-x")
    }
    filePermissions {
      // 0b110110100
      unix("rw-rw-r--")
    }
  }

  // Run the checkBestPractices check for build-logic included builds.
  register("checkBuildLogicBestPractices") {
    description = "Run the checkBestPractices check for build-logic included builds!"
    group = BasePlugin.BUILD_GROUP
    dependsOn(gradle.includedBuild("build-logic").task(":checkBestPractices"))
  }

  // Clean all composite builds
  register("cleanAll") {
    description = "Clean all projects including composite builds"
    group = LifecycleBasePlugin.CLEAN_TASK_NAME

    dependsOn(gradle.includedBuilds.map { it.task(":cleanAll") })
    subprojects.mapNotNull { it.tasks.findByName("clean") }.forEach { dependsOn(it) }
  }

  wrapper {
    gradleVersion = libs.versions.gradle.asProvider().get()
    distributionType = Wrapper.DistributionType.ALL
    // distributionUrl = "${Repo.GRADLE_DISTRO}/gradle-$gradleVersion-bin.zip"
  }

  defaultTasks("clean", "tasks", "--all")
}
