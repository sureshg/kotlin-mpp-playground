@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.devtools.ksp.gradle.KspAATask
import common.*
import kotlinx.validation.*
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.*
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.*
import org.jetbrains.kotlin.gradle.targets.wasm.npm.*
import tasks.*

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  kotlin("plugin.power-assert")
  kotlin("plugin.js-plain-objects")
  id("dev.suresh.plugin.common")
  id("dev.suresh.plugin.kotlin.docs")
  com.google.devtools.ksp
  dev.zacsweers.redacted
  // com.javiersc.kotlin.kopy
  // kotlin("plugin.compose")
  // org.gradle.kotlin.`kotlin-dsl`
}

configurations.configureEach { resolutionStrategy { failOnNonReproducibleResolution() } }

kotlin {
  commonTarget(project)
  when (project.name) {
    sharedProjectName -> {
      jvmTarget(project)
      jsTarget(project)
      wasmJsTarget(project)
      nativeTargets(project) {}
    }

    "web",
    "frontend" -> {
      jsTarget(project)
      wasmJsTarget(project)
    }
  }

  // applyDefaultHierarchyTemplate {
  //    common {
  //      group("posix") {
  //        // Using group will add the intermediate source sets
  //        group("linux")
  //        group("apple")
  //      }
  //
  //     group("jsAndWasmShared") {
  //        withJs()
  //        withWasmJs()
  //       withWasmWasi()
  //      }
  //    }
  //  }

  // ==== To configure specific targets ====
  // targets.withType<KotlinJvmTarget>().configureEach { compilerOptions {} }
  // targets.matching { it.platformType == js }.configureEach { apply(plugin = ...) }

  // kotlinDaemonJvmArgs = defaultJvmArgs
  // explicitApiWarning()
}

ksp {
  allWarningsAsErrors = false
  // excludedSources.from(generateCodeTask)
}

powerAssert {
  functions =
      listOf(
          "kotlin.assert",
          "kotlin.test.assertTrue",
          "kotlin.test.assertEquals",
          "kotlin.test.assertNull",
          "kotlin.require")
}

redacted {
  enabled = true
  replacementString = "█"
}

// kopy { copyFunctions = listOf(KopyCopyFunctions.Copy) }

tasks {
  val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
  val buildConfig = register<BuildConfig>("buildConfig", buildConfigExtn)
  buildConfig.configure { enabled = buildConfigExtn.enabled.get() }
  kotlin.sourceSets.commonMain { kotlin.srcDirs(buildConfig) }
  // compileKotlinMetadata { dependsOn(buildConfig) }

  withType<KspAATask>().configureEach { configureKspConfig() }

  withType<KotlinNpmInstallTask>().configureEach { configureKotlinNpm() }

  // withType<Kotlin2JsCompile>().configureEach {}

  withType<Jar>().configureEach {
    manifest { attributes(defaultJarManifest) }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }

  withType<ProcessResources>().configureEach {
    val version = project.version.toString()
    val rootProjectName = rootProject.name
    val moduleName = project.name

    inputs.property("version", version)
    filesMatching("**/*-res.txt") {
      expand(
          "name" to rootProjectName,
          "module" to moduleName,
          "version" to version,
      )
    }
    filesMatching("**/*.yaml") {
      filter { line ->
        line.replace("{project.name}", rootProjectName).replace("{project.version}", version)
      }
    }
  }

  pluginManager.withPlugin("com.gradleup.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          // https://gradleup.com/shadow/kotlin-plugins/
          jarFile = named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
          javaOpts = runJvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file(project.name) }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }

    // Shows how to register a shadowJar task for the default jvm target
    register<ShadowJar>("shadowJvmJar") {
      val main by kotlin.jvm().compilations
      // allOutputs == classes + resources
      from(main.output.allOutputs)
      val runtimeDepConfig =
          project.configurations.getByName(main.runtimeDependencyConfigurationName)
      configurations = listOf(runtimeDepConfig)
      archiveClassifier = "jvm-all"
      mergeServiceFiles()
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
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
      // inputJar = named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
    }
  }
}

// var npmEnabled: String? by rootProject.extra

plugins.withType<NodeJsPlugin> {
  the<NodeJsEnvSpec>().apply {
    download = true
    // version = libs.versions.nodejs.version.get()
    // downloadBaseUrl = "https://nodejs.org/download/nightly"
  }

  rootProject.the<NpmExtension>().apply {
    lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
    packageLockMismatchReport = LockFileMismatchReport.WARNING
    packageLockAutoReplace = false
  }
}

plugins.withType<WasmNodeJsPlugin> {
  the<WasmNodeJsEnvSpec>().apply {
    download = true
    // version = libs.versions.nodejs.version.get()
    // downloadBaseUrl = "https://nodejs.org/download/nightly"
  }
  rootProject.the<WasmNpmExtension>().apply {
    lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store/wasm")
    packageLockMismatchReport = LockFileMismatchReport.WARNING
    packageLockAutoReplace = false
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

dependencies {
  // add("kspJvm", project(":ksp-processor"))
}
