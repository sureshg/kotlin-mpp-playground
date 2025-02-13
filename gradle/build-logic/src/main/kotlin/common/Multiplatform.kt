package common

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

fun KotlinMultiplatformExtension.commonTarget(project: Project) =
    with(project) {
      jvmToolchain { configureJvmToolchain(project) }
      compilerOptions { configureKotlinCommon(project) }
      withSourcesJar(publish = true)
      // targets.configureEach {}

      sourceSets {
        all {
          // Apply multiplatform library bom to all source sets
          dependencies {
            api(project.dependencies.platform(libs.kotlin.bom))
            api(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            api(project.dependencies.platform(libs.kotlinx.serialization.bom))
            api(project.dependencies.platform(libs.ktor.bom))
            api(project.dependencies.platform(libs.kotlin.wrappers.bom))
          }
          // languageSettings.enableLanguageFeature(LanguageFeature.xx)
        }

        commonMain {
          dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.io.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.serialization.json.io)
            api(libs.kotlinx.collections.immutable)
            api(libs.kotlin.redacted.annotations)
            api(libs.kotlin.retry)
            api(libs.kotlin.logging)
            api(libs.kotlinx.html)
            api(libs.bundles.ajalt)
            api(libs.kotlinx.jsonpath)
            api(libs.kotlin.cryptography.core)
            api(libs.kotlin.cryptography.random)
            api(libs.kotlin.bignum)
            api(libs.kotlin.bignum.serialization)
            api(libs.ktor.client.core)
            api(libs.ktor.client.cio)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.encoding)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.resources)
            api(libs.ktor.client.auth)
            api(libs.ktor.client.serialization)
            api(libs.ktor.client.websockets)
            api(libs.ktor.serialization.json)
          }
          // kotlin.srcDirs()
          // resources.srcDirs()
        }

        commonTest {
          dependencies {
            api(kotlin("test"))
            api(libs.kotlinx.coroutines.test)
            api(libs.ktor.client.mock)
            api(libs.cash.turbine)
          }
        }

        // Get target compilations
        // val commonTarget = targets.first { it.platformType == KotlinPlatformType.common }
        // OR targets["metadata"]
        // val compilation = commonTarget.compilations["main"]

        // Add a task output as sourceSet
        // compilation.defaultSourceSet.kotlin.srcDir(buildConfig)

        // Add new sourceSet
        // val newSourceSet = sourceSets.create("gen")
        // compilation.defaultSourceSet.dependsOn(newSourceSet)
      }
    }

fun KotlinMultiplatformExtension.jvmTarget(project: Project) =
    with(project) {
      jvm {
        withJava()
        compilations.configureEach {
          compileJavaTaskProvider?.configure { configureJavac(project) }
        }
        compilerOptions { configureKotlinJvm(project) }

        mainRun {
          mainClass = libs.versions.app.mainclass
          setArgs(jvmRunArgs)
        }

        // val test by testRuns.existing
        testRuns.configureEach { executionTask.configure { configureJavaTest() } }

        // Configure application executable only it's enabled
        if (isKmpExecEnabled) {
          binaries {
            executable {
              mainClass = libs.versions.app.mainclass
              applicationDefaultJvmArgs = jvmRunArgs
              applicationDistribution.duplicatesStrategy = DuplicatesStrategy.WARN
            }
          }
        }

        // Register a task to execute a class using jvm runtime dependencies.
        // compilations.getByName("test") {
        //   tasks.register<JavaExec>("ktExec") {
        //     classpath(runtimeDependencyFiles, output)
        //     mainClass = "dev.suresh.test.ExecMain"
        //   }
        // }

        // attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
      }

      sourceSets {
        jvmMain {
          // dependsOn(jvmCommon)
          dependencies {
            // api(libs.kotlin.stdlib)
            api(libs.kotlin.metadata.jvm)
            api(libs.ktor.client.java)
            api(libs.slf4j.api)
            api(libs.slf4j.jul)
            api(libs.kotlinx.coroutines.slf4j)
            api(libs.jspecify)
            api(libs.bundles.keystore)
            // https://kotlinlang.org/docs/ksp-multiplatform.html
            api(libs.google.auto.annotations)
            ksp(libs.ksp.auto.service)
          }
        }

        jvmTest {
          dependencies {
            api(project.dependencies.platform(libs.junit.bom))
            api(project.dependencies.platform(libs.testcontainers.bom))
            api(kotlin("test-junit5"))
            api(libs.mockk)
            api(libs.slf4j.simple)
            api(libs.testcontainers.junit5)
            api(libs.testcontainers.postgresql)
            // api(kotlin("reflect"))
            // api(libs.konsist)
          }
        }
      }
    }

fun KotlinMultiplatformExtension.jsTarget(project: Project) =
    with(project) {
      js {
        browser {
          commonWebpackConfig {
            cssSupport { enabled = true }
            // outputFileName = "js-app.js"
            // scssSupport { enabled = true }
            // sourceMaps = true
          }

          runTask { sourceMaps = false }
          testTask {
            enabled = true
            testLogging { configureLogEvents() }
            useKarma { useChromeHeadless() }
          }

          // distribution { outputDirectory = file("$projectDir/docs") }
        }

        if (isSharedProject.not()) {
          binaries.executable()
        }
        generateTypeScriptDefinitions()
        compilerOptions { configureKotlinJs() }
        testRuns.configureEach { executionTask.configure {} }
      }

      sourceSets {
        jsMain {
          dependencies {
            api(libs.kotlin.wrappers.browser)
            api(libs.kotlin.wrappers.css)
            // api(npm("@js-joda/timezone", libs.versions.npm.jsjoda.tz.get()))
            // ksp(project(":meta:ksp:processor"))
          }

          // kotlin.srcDir("src/main/kotlin")
          // resources.srcDir("src/main/resources")
        }

        jsTest { kotlin {} }
      }
    }

fun KotlinMultiplatformExtension.wasmJsTarget(project: Project) =
    with(project) {
      wasmJs {
        // moduleName = "wasm-app"
        browser {
          val rootDirPath = project.rootDir.path
          val projectDirPath = project.projectDir.path
          commonWebpackConfig {
            cssSupport { enabled = true }
            // outputFileName = "wasm-app.js"
            // sourceMaps = true
            devServer =
                (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                  static =
                      (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside the browser
                        add(projectDirPath)
                        add(rootDirPath)
                      }
                }
          }

          runTask { sourceMaps = false }
          testTask {
            enabled = true
            testLogging { configureLogEvents() }
            useKarma { useChromeHeadless() }
          }
        }

        if (isSharedProject.not()) {
          binaries.executable()
        }
        generateTypeScriptDefinitions()
        compilerOptions { configureKotlinJs() }
        testRuns.configureEach { executionTask.configure {} }
      }

      sourceSets {
        wasmJsMain {
          dependencies {
            // kotlinx-browser is only supported for WasmJs.
            api(libs.kotlinx.browser)
            // api(npm("@js-joda/timezone", libs.versions.npm.jsjoda.tz.get()))
          }
        }
        wasmJsTest { kotlin {} }
      }
    }

fun KotlinMultiplatformExtension.hostNativeTarget(configure: KotlinNativeTarget.() -> Unit = {}) =
    when {
      Platform.isMac -> {
        macosArm64 { configure() }
        macosX64 { configure() }
      }
      Platform.isLinux -> {
        linuxArm64 { configure() }
        linuxX64 { configure() }
      }
      Platform.isWin -> mingwX64 { configure() }
      else ->
          throw GradleException("Host OS '${Platform.currentOS}' is not supported in Kotlin/Native")
    }

fun KotlinMultiplatformExtension.nativeTargets(
    project: Project,
    configure: KotlinNativeTarget.() -> Unit = {}
) =
    with(project) {
      val nativeBuild: String? by project
      val nativeWinTarget: String? by project

      if (nativeBuild.toBoolean()) {
        fun KotlinNativeTarget.configureAll() {
          compilerOptions {
            // freeCompilerArgs.addAll("-Xverbose-phases=Linker", "-Xruntime-logs=gc=info")
          }
          configure()
        }

        compilerOptions {
          optIn.addAll(
              "kotlinx.cinterop.ExperimentalForeignApi",
              "kotlin.experimental.ExperimentalNativeApi",
          )
        }

        macosX64 { configureAll() }
        macosArm64 { configureAll() }
        linuxX64 { configureAll() }
        linuxArm64 { configureAll() }
        if (nativeWinTarget.toBoolean()) {
          mingwX64 { configureAll() }
        }

        sourceSets { nativeMain { dependencies { api(libs.ktor.client.curl) } } }
      }
    }

fun KotlinMultiplatformExtension.addKspDependencyForAllTargets(
    project: Project,
    dependencyNotation: Any,
    configurationNameSuffix: String = ""
) =
    with(project) {
      // val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
      targets
          .filter { target ->
            // Don't add KSP for common target, only final platforms
            target.platformType != KotlinPlatformType.common
          }
          .forEach { target ->
            dependencies.add(
                "ksp${target.targetName.replaceFirstChar { it.uppercaseChar() }}$configurationNameSuffix",
                dependencyNotation,
            )
          }
    }
