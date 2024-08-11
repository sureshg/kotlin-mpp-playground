package common

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

context(Project)
fun KotlinMultiplatformExtension.commonTarget() {
  jvmToolchain { configureJvmToolchain() }
  compilerOptions { configureKotlinCommon() }
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

context(Project)
fun KotlinMultiplatformExtension.jvmTarget() {
  jvm {
    withJava()
    compilations.configureEach { compileJavaTaskProvider?.configure { configureJavac() } }
    compilerOptions { configureKotlinJvm() }

    mainRun {
      mainClass = libs.versions.app.mainclass
      setArgs(jvmRunArgs)
    }

    // val test by testRuns.existing
    testRuns.configureEach { executionTask.configure { configureJavaTest() } }

    // attributes.attribute(mppTargetAttr, platformType.name)
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
        api(libs.kotlinx.coroutines.slf4j)
        api(libs.jspecify)
        api(libs.password4j)
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
        // api(libs.konsist)
      }
    }
  }
}

context(Project)
fun KotlinMultiplatformExtension.jsTarget() {
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
    testRuns.configureEach { executionTask.configure { configureTestReport() } }
  }

  sourceSets {
    jsMain {
      dependencies {
        api(libs.ktor.client.js)
        api(kotlinw("browser"))
        api(kotlinw("css"))
        // api(npm("@js-joda/timezone", libs.versions.npm.jsjoda.tz.get()))
        // ksp(project(":meta:ksp:processor"))
      }

      // kotlin.srcDir("src/main/kotlin")
      // resources.srcDir("src/main/resources")
    }

    jsTest { kotlin {} }
  }
}

context(Project)
fun KotlinMultiplatformExtension.wasmJsTarget() {
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
    testRuns.configureEach { executionTask.configure { configureTestReport() } }
  }

  sourceSets {
    wasmJsMain {
      dependencies {
        // kotlinx-browser is only supported for WasmJs.
        api(libs.kotlinx.browser)
        api(libs.ktor.client.js)
        // api(npm("@js-joda/timezone", libs.versions.npm.jsjoda.tz.get()))
      }
    }
    wasmJsTest { kotlin {} }
  }
}

context(Project)
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
          throw GradleException(
              "Host OS '${Platform.currentOS}' is not supported in Kotlin/Native $project.")
    }

context(Project)
fun KotlinMultiplatformExtension.allNativeTargets(configure: KotlinNativeTarget.() -> Unit = {}) {
  val nativeBuild: String? by project
  if (nativeBuild.toBoolean()) {
    macosX64 { configure() }
    macosArm64 { configure() }
    linuxX64 { configure() }
    linuxArm64 { configure() }

    val nativeWinTarget: String? by project
    if (nativeWinTarget.toBoolean()) {
      mingwX64 { configure() }
    }
  }
}

context(Project)
fun KotlinMultiplatformExtension.addKspDependencyForAllTargets(
    dependencyNotation: Any,
    configurationNameSuffix: String = ""
) {
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
