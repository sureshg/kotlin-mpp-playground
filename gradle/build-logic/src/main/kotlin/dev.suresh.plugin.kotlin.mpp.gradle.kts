@file:Suppress("UnstableApiUsage")

import com.google.devtools.ksp.gradle.KspAATask
import common.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Attributes
import kotlinx.validation.*
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import tasks.*

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  kotlin("plugin.power-assert")
  kotlin("plugin.js-plain-objects")
  id("dev.suresh.plugin.kotlin.docs")
  com.google.devtools.ksp
  dev.zacsweers.redacted
  // com.javiersc.kotlin.kopy
  org.jetbrains.kotlinx.atomicfu
  // kotlin("plugin.atomicfu")
  // kotlin("plugin.compose")
  // io.github.terrakok.`kmp-hierarchy`
  // org.gradle.kotlin.`kotlin-dsl`
  // app.cash.molecule
  // dev.mokkery
}

val rootProjectName = rootProject.name
val projectName = project.name
val projectVersion = project.version.toString()
val projectGroup = project.group.toString()
val gradleVersion = gradle.gradleVersion

kotlin {
  commonTarget()
  when (projectName) {
    sharedProjectName -> {
      jvmTarget()
      jsTarget()
      wasmJsTarget()
      allNativeTargets { compilerOptions { configureKotlinNative() } }
    }
    "native" -> allNativeTargets { compilerOptions { configureKotlinNative() } }
    "web" -> {
      jsTarget()
      wasmJsTarget()
    }
    "cmp" -> {
      jvmTarget()
      wasmJsTarget()
    }
    "html" -> jsTarget()
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

atomicfu {
  transformJvm = true
  transformJs = true
  jvmVariant = "VH"
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
  allWarningsAsErrors = false
  // excludedSources.from(generateCodeTask)
}

powerAssert { functions = listOf("kotlin.assert", "kotlin.test.assertTrue") }

redacted {
  enabled = true
  replacementString = "█"
}

// kopy { functions = KopyFunctions.Copy }

tasks {
  // Register buildConfig task only for project's common module
  if (project.isSharedProject) {
    val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
    val buildConfig by register<BuildConfig>("buildConfig", buildConfigExtn)
    kotlin.sourceSets.commonMain { kotlin.srcDirs(buildConfig) }
    // compileKotlinMetadata { dependsOn(buildConfig) }
    // maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
  }

  withType<KspAATask>().configureEach { configureKspConfig() }

  withType<KotlinNpmInstallTask>().configureEach { configureKotlinNpm() }

  // withType<Kotlin2JsCompile>().configureEach {}

  withType<Jar>().configureEach {
    manifest {
      attributes(
          // "Automatic-Module-Name" to project.group,
          "Enable-Native-Access" to "ALL-UNNAMED",
          "Built-By" to System.getProperty("user.name"),
          "Built-Jdk" to System.getProperty("java.runtime.version"),
          "Built-OS" to
              "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
          "Build-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now()),
          "Created-By" to "Gradle $gradleVersion",
          Attributes.Name.IMPLEMENTATION_TITLE.toString() to projectName,
          Attributes.Name.IMPLEMENTATION_VERSION.toString() to projectVersion,
          Attributes.Name.IMPLEMENTATION_VENDOR.toString() to projectGroup,
      )
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
  }

  withType<ProcessResources>().configureEach {
    inputs.property("version", projectVersion)
    filesMatching("**/*-res.txt") {
      expand(
          "name" to rootProjectName,
          "module" to projectName,
          "version" to projectVersion,
      )
    }
    filesMatching("**/*.yaml") {
      filter { line ->
        line.replace("{project.name}", rootProjectName).replace("{project.version}", projectVersion)
      }
    }
  }

  pluginManager.withPlugin("org.jetbrains.kotlinx.binary-compatibility-validator") {
    configure<ApiValidationExtension> {
      ignoredPackages.add("dev.suresh.test")
      ignoredClasses.addAll(listOf("BuildConfig", "BuildConfig\$Host"))
      validationDisabled = true
      klib { enabled = true }
    }

    withType<KotlinApiBuildTask>().configureEach {
      // inputJar = named<Jar>("shadowJar").flatMap { it.archiveFile }
    }
  }

  pluginManager.withPlugin("com.gradleup.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          jarFile = named<Jar>("shadowJar").flatMap { it.archiveFile }
          // javaOpts = application.applicationDefaultJvmArgs
          javaOpts = named<JavaExec>("run").get().jvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file("${projectName}-app") }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }
  }
}

// Expose shared js/wasm resource as configuration to be consumed by other projects.
// https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects
artifacts {
  if (isSharedProject) {
    tasks.findByName("jsProcessResources")?.let {
      val sharedJsResources by configurations.consumable("sharedJsResources")
      add(sharedJsResources.name, provider { it })
    }

    tasks.findByName("wasmJsProcessResources")?.let {
      val sharedWasmResources by configurations.consumable("sharedWasmResources")
      add(sharedWasmResources.name, provider { it })
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
