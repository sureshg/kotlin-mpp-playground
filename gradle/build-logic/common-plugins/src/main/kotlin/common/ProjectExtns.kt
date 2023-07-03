package common

import java.io.ByteArrayOutputStream
import java.nio.file.Path
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskContainer
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*

/** Returns version catalog of this project. */
internal val Project.libs
  get() = the<LibrariesForLibs>()

/**
 * Returns version catalog extension of this project. Give access to all version catalogs available.
 */
internal val Project.catalogs
  get() = the<VersionCatalogsExtension>()

// val logger = LoggerFactory.getLogger("build-logic")

/** Quote for -Xlog file */
val Project.xQuote
  get() = if (OperatingSystem.current().isWindows) """\"""" else """""""

val Project.commonProjectName
  get() = "common"

val Project.isPlatformProject
  get() = plugins.hasPlugin("java-platform")

val Project.isJavaLibraryProject
  get() = plugins.hasPlugin("java-library")

// val debug: String? by project
val Project.debugEnabled
  get() = properties["debug"]?.toString().toBoolean()

val Project.hasCleanTask
  get() = gradle.startParameter.taskNames.any { it == "clean" }

/** Checks if the project has a snapshot version. */
val Project.isSnapshot
  get() = version.toString().endsWith("SNAPSHOT", true)

val Project.runsOnCI
  get() = providers.environmentVariable("CI").getOrElse("false").toBoolean()

/** Check if it's a non-stable(RC) version. */
val String.isNonStable: Boolean
  get() {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[\\d,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
  }

/** Returns the JDK install path provided by the [JavaToolchainService] */
val Project.javaToolchainPath
  get(): Path {
    val defToolchain = extensions.findByType(JavaPluginExtension::class)?.toolchain
    val javaToolchainSvc = extensions.findByType(JavaToolchainService::class)
    // val jvm: JavaVersion? = org.gradle.internal.jvm.Jvm.current().javaVersion

    val jLauncher =
        when (defToolchain != null) {
          true -> javaToolchainSvc?.launcherFor(defToolchain)
          else -> javaToolchainSvc?.launcherFor { languageVersion = toolchainVersion }
        }?.orNull

    return jLauncher?.metadata?.installationPath?.asFile?.toPath()
        ?: error("Requested JDK version ($javaVersion) is not available.")
  }

/** Lazy version of [TaskContainer.maybeCreate] */
inline fun <reified T : Task> TaskContainer.maybeRegister(
    taskName: String,
    noinline configAction: T.() -> Unit
) =
    when (taskName) {
      in names -> named(taskName, T::class)
      else -> register(taskName, T::class)
    }.also { it.configure(configAction) }

/** Return incubator modules of the tool chain JDK */
val Project.incubatorModules
  get(): String {
    val javaCmd = project.javaToolchainPath.resolve("bin").resolve("java")
    val bos = ByteArrayOutputStream()
    val execResult = exec {
      workingDir = layout.buildDirectory.get().asFile
      commandLine = listOf(javaCmd.toString())
      args = listOf("--list-modules")
      standardOutput = bos
      errorOutput = bos
    }
    execResult.assertNormalExitValue()
    return bos.toString(Charsets.UTF_8)
        .lines()
        .filter { it.startsWith("jdk.incubator") }
        .joinToString(",") { it.substringBefore("@").trim() }
  }

/**
 * Print all the catalog version strings and it's values.
 *
 * [VersionCatalogsExtension](https://docs.gradle.org/current/userguide/platforms.html#sub:type-unsafe-access-to-catalog)
 */
fun Project.printVersionCatalog() {
  if (debugEnabled) {
    catalogs.catalogNames.map { cat ->
      println("=== Catalog $cat ===")
      val catalog = catalogs.named(cat)
      catalog.versionAliases.forEach { alias ->
        println("${alias.padEnd(30, '-')}-> ${catalog.findVersion(alias).get()}")
      }
    }
  }
}

/** Print all the tasks */
fun Project.printTaskGraph() {
  if (debugEnabled) {
    gradle.taskGraph.whenReady {
      allTasks.forEachIndexed { index, task -> println("${index + 1}. ${task.name}") }
    }
  }
}
