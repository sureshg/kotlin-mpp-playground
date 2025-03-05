@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalBCVApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.devtools.ksp.gradle.KspAATask
import com.javiersc.kotlin.kopy.args.KopyFunctions
import common.*
import java.util.jar.Attributes
import kotlinx.validation.*
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
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
  com.javiersc.kotlin.kopy
  org.jetbrains.kotlinx.atomicfu
  // kotlin("plugin.atomicfu")
  // kotlin("plugin.compose")
  // io.github.terrakok.`kmp-hierarchy`
  // org.gradle.kotlin.`kotlin-dsl`
  // app.cash.molecule
  // dev.mokkery
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
  //      group("jsCommon") {
  //        withJs()
  //        withWasmJs()
  //      }
  //    }
  //  }

  // ==== To configure specific targets ====
  // targets.withType<KotlinJvmTarget>().configureEach { compilerOptions {} }
  // targets.matching { it.platformType == js }.configureEach { apply(plugin = ...) }

  // kotlinDaemonJvmArgs = jvmArguments
  // explicitApiWarning()
}

atomicfu {
  transformJvm = true
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

kopy { functions = KopyFunctions.Copy }

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
    val shadowJar by
        existing(ShadowJar::class) {
          // https://gradleup.com/shadow/kmp-plugin/
          manifest {
            attributes[Attributes.Name.MAIN_CLASS.toString()] = libs.versions.app.mainclass
          }
        }

    val buildExecutable by
        registering(ReallyExecJar::class) {
          jarFile = shadowJar.flatMap { it.archiveFile }
          javaOpts = jvmRunArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file("${project.name}-app") }
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
      manifest { attributes[Attributes.Name.MAIN_CLASS.toString()] = libs.versions.app.mainclass }
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
      // inputJar = named<Jar>("shadowJar").flatMap { it.archiveFile }
    }
  }
}

var npmEnabled: String? by rootProject.extra

plugins.withType<NodeJsPlugin> {
  the<NodeJsEnvSpec>().apply {
    download = true
    // version = libs.versions.nodejs.version.get()
    // downloadBaseUrl = "https://nodejs.org/download/nightly"
  }

  if (!npmEnabled.toBoolean()) {
    rootProject.the<NpmExtension>().apply {
      lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      packageLockMismatchReport = LockFileMismatchReport.WARNING
      packageLockAutoReplace = false
    }
    npmEnabled = "true"
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
