@file:Suppress("UnstableApiUsage")

package plugins

import com.google.devtools.ksp.gradle.KspAATask
import common.*
import java.util.jar.Attributes
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import tasks.BuildConfig
import tasks.BuildConfigExtension
import tasks.ReallyExecJar

plugins {
  java
  `kotlin-multiplatform`
  `kotlinx-serialization`
  com.google.devtools.ksp
  `kotlinx-atomicfu`
  dev.zacsweers.redacted
  id("plugins.kotlin.docs")
  kotlin("plugin.power-assert")
  kotlin("plugin.js-plain-objects")
  // kotlin("plugin.compose")
  // io.github.terrakok.`kmp-hierarchy`
  // org.gradle.kotlin.`kotlin-dsl`
  // app.cash.molecule
  // dev.mokkery
}

kotlin {
  commonTarget()
  when (project.name) {
    sharedProjectName -> {
      jvmTarget()
      jsTarget()
      allNativeTargets { compilerOptions { configureKotlinNative() } }
      // wasmJsTarget()
    }
    "js",
    "chrome",
    "html" -> jsTarget()
    "wasm" -> wasmJsTarget()
    "native" -> allNativeTargets { compilerOptions { configureKotlinNative() } }
    else -> jvmTarget()
  }

  applyDefaultHierarchyTemplate {
    common {
      group("posix") {
        // Using group will add the intermediate source sets
        group("linux")
        group("apple")
      }

      group("jsCommon") {
        withJs()
        withWasmJs()
      }
    }
  }

  // To configure specific targets
  targets.withType<KotlinJvmTarget>().configureEach { compilerOptions {} }
  // targets.matching { it.platformType == js }.configureEach { apply(plugin = ...) }

  // kotlinDaemonJvmArgs = jvmArguments
  // explicitApiWarning()
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
  allWarningsAsErrors = false
  // excludedSources.from(generateCodeTask)
}

atomicfu {
  transformJvm = true
  transformJs = true
  jvmVariant = "VH"
}

powerAssert { functions = listOf("kotlin.assert", "kotlin.test.assertTrue") }

redacted {
  enabled = true
  replacementString = "█"
}

kover {
  // useJacoco()
  reports {
    total {
      filters { excludes { classes("dev.suresh.example.*") } }
      html { title = "${project.name} code coverage report!" }
      verify {
        rule {
          groupBy = GroupingEntityType.APPLICATION
          minBound(0, CoverageUnit.LINE)
          minBound(0, CoverageUnit.BRANCH)
        }
        warningInsteadOfFailure = true
      }
    }
  }
}

tasks {
  // Register buildConfig task only for common module
  if (project.isSharedProject) {
    val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
    val buildConfig by register<BuildConfig>("buildConfig", buildConfigExtn)
    kotlin.sourceSets.commonMain { kotlin.srcDirs(buildConfig) }
    // compileKotlinMetadata { dependsOn(buildConfig) }
    // maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
  }

  withType<KspAATask>().configureEach { configureKspConfig() }

  withType<KotlinNpmInstallTask>().configureEach { configureKotlinNpm() }

  // withType<KotlinJsCompile>().configureEach { }

  withType<Jar>().configureEach {
    manifest {
      attributes(
          "Automatic-Module-Name" to project.group,
          "Built-By" to System.getProperty("user.name"),
          "Built-JDK" to System.getProperty("java.runtime.version"),
          "Built-OS" to System.getProperty("os.name"),
          "Created-By" to "Gradle ${gradle.gradleVersion}",
          Attributes.Name.IMPLEMENTATION_TITLE.toString() to project.name,
          Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version,
          Attributes.Name.IMPLEMENTATION_VENDOR.toString() to project.group,
      )
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
  }

  withType<ProcessResources>().configureEach {
    inputs.property("version", project.version.toString())
    filesMatching("*-res.txt") {
      expand(
          "name" to project.name,
          "version" to project.version,
      )
    }
  }

  pluginManager.withPlugin("com.github.johnrengelman.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          jarFile = named<Jar>("shadowJar").flatMap { it.archiveFile }
          // javaOpts = application.applicationDefaultJvmArgs
          javaOpts = named<JavaExec>("run").get().jvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file("${project.name}-app") }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }
  }
}

// Expose shared js/wasm resource as configuration to be consumed by other projects.
// Should we really need this?, revisit this later.
// https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects
artifacts {
  if (isSharedProject) {
    tasks.findByName("jsProcessResources")?.let {
      val sharedJsRes by configurations.consumable("sharedJsResources")
      add(sharedJsRes.name, tasks.named(it.name))
    }
    tasks.findByName("wasmJsProcessResources")?.let {
      val sharedWasmRes by configurations.consumable("sharedWasmResources")
      add(sharedWasmRes.name, tasks.named(it.name))
    }
  }
}

// Initialize Node.js and NPM extensions only once in a multi-module project
var nodeJsEnabled: String? by rootProject.extra

if (nodeJsEnabled.toBoolean().not()) {
  rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.extensions.configure<NodeJsRootExtension> {
      download = true
      nodeJsEnabled = "true"
      // version = libs.versions.nodejs.version.get()
      // nodeDownloadBaseUrl = "https://nodejs.org/download/nightly"
    }

    rootProject.extensions.configure<NpmExtension> {
      lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      packageLockMismatchReport = LockFileMismatchReport.WARNING
      packageLockAutoReplace = false
      nodeJsEnabled = "true"
    }
  }
}

dependencies {
  // add("kspJvm", project(":ksp-processor"))
}
