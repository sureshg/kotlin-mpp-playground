package plugins

import com.github.ajalt.mordant.rendering.TextColors.*
import common.*
import org.gradle.kotlin.dsl.*

plugins {
  id("plugins.common")
  idea
  wrapper
  com.github.`ben-manes`.versions
  dev.iurysouza.modulegraph
  id("plugins.kotlin.docs")
  id("plugins.publishing")
  // id("gg.jte.gradle")
}

if (hasCleanTask) {
  logger.warn(
      yellow(
              """
            | CLEANING ALMOST NEVER FIXES YOUR BUILD!
            | Cleaning is often a last-ditch effort to fix perceived build problems that aren't going to
            | actually be fixed by cleaning. What cleaning will do though is make your next few builds
            | significantly slower because all the incremental compilation data has to be regenerated,
            | so you're really just making your day worse.
            """)
          .trimMargin(),
  )
}

gradle.projectsEvaluated { logger.lifecycle(magenta("=== Projects Configuration Completed ===")) }

idea {
  module {
    isDownloadJavadoc = false
    isDownloadSources = false
  }
  project.vcs = "Git"
}

moduleGraphConfig {
  readmePath = "./README.md"
  heading = "### Module Dependency"
}

// jte {
//   contentType = ContentType.Plain
//   generateNativeImageResources = true
//   generate()
// }

// Skip test tasks on skip.test=true
if (skipTest) {
  allprojects {
    tasks
        .matching { it.name.endsWith("test", ignoreCase = true) }
        .configureEach { onlyIf { false } }
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
    dirPermissions {
      // 0b111111101
      unix("rwxrwxr-x")
    }
    filePermissions {
      // 0b110110100
      unix("rw-rw-r--")
    }
  }

  // Set up git hooks
  register<Copy>("setUpGitHooks") {
    group = "help"
    from("$rootDir/gradle/.githooks")
    into("$rootDir/.git/hooks")
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
    allprojects.mapNotNull { it.tasks.findByName("clean") }.forEach { dependsOn(it) }
  }

  wrapper {
    gradleVersion = libs.versions.gradle.asProvider().get()
    distributionType = Wrapper.DistributionType.ALL
    // distributionUrl = "${Repo.GRADLE_DISTRO}/gradle-$gradleVersion-bin.zip"
  }

  defaultTasks("clean", "tasks", "--all")
}
