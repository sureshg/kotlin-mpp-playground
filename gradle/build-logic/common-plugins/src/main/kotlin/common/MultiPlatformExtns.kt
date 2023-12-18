package common

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

context(Project)
fun KotlinMultiplatformExtension.commonTarget() {
  jvmToolchain { configureJvmToolchain() }
  withSourcesJar(publish = true)

  targets.all {
    // Configure all compilations of all targets
    compilations.all {
      compileTaskProvider.configure { compilerOptions { configureKotlinCommon() } }
      // compilerOptions.configure { configureKotlinCommon() }
    }
  }

  sourceSets {
    all {
      languageSettings { configureKotlinLang() }
      // Apply multiplatform library bom to all source sets
      dependencies {
        api(project.dependencies.platform(libs.kotlin.bom))
        api(project.dependencies.platform(libs.ktor.bom))
        api(project.dependencies.platform(libs.kotlin.wrappers.bom))
      }
    }

    commonMain {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.atomicfu)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.collections.immutable)
        api(libs.kotlin.redacted.annotations)
        api(libs.kotlinx.io.core)
        api(libs.ktor.client.core)
        api(libs.ktor.client.content.negotiation)
        api(libs.ktor.client.encoding)
        api(libs.ktor.client.logging)
        api(libs.ktor.client.resources)
        api(libs.ktor.client.auth)
        api(libs.ktor.serialization.json)
        api(libs.kotlin.logging)
      }
    }

    commonTest {
      dependencies {
        api(kotlin("test"))
        api(libs.kotlinx.coroutines.test)
        api(libs.cash.turbine)
        api(libs.ktor.client.mock)
      }
    }

    // val jvmCommon by creating {
    //   dependsOn(commonMain.get())
    //   dependencies {
    //     implementation(..)
    //   }
    // }

    // val target = targets.first { it.platformType == KotlinPlatformType.common }
    // val compilation = target.compilations["main"]
    // // OR val compilation = targets["metadata"].compilations["main"]
    // compilation.defaultSourceSet.kotlin.srcDir(buildConfig)
    // // val newSourceSet = sourceSets.create("gen")
    // // compilation.defaultSourceSet.dependsOn(newSourceSet)
  }
}

context(Project)
fun KotlinMultiplatformExtension.jvmTarget() {
  jvm {
    withJava()
    // withSourcesJar(publish = false)
    compilations.all {
      compileJavaTaskProvider?.configure { configureJavac() }
      compileTaskProvider.configure { compilerOptions { configureKotlinJvm() } }
      // compilerOptions.configure { configureKotlinJvm() }
    }

    // ./gradlew jvmRun
    mainRun { mainClass = libs.versions.app.mainclass.get() }
    // val test by testRuns.existing
    testRuns.configureEach { executionTask.configure { configureKotlinTest() } }

    // attributes.attribute(mppTargetAttr, "jvm")
  }

  sourceSets {
    jvmMain {
      // dependsOn(jvmCommon)
      dependencies {
        // api(libs.kotlin.stdlib)
        api(libs.kotlinx.metadata.jvm)
        api(libs.google.auto.annotations)
        api(libs.ktor.client.java)
        api(libs.kotlin.retry)
        api(libs.slf4j.api)
        api(libs.kotlinx.coroutines.slf4j)
        api(libs.jspecify)
        // https://kotlinlang.org/docs/ksp-multiplatform.html
        kspDependency("jvm", libs.ksp.auto.service)
      }
    }

    jvmTest {
      dependencies {
        api(project.dependencies.platform(libs.junit.bom))
        api(project.dependencies.platform(libs.testcontainers.bom))
        api(kotlin("test-junit5"))
        api(libs.slf4j.simple)
        api(libs.mockk)
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
    useEsModules()
    binaries.executable()
    // binaries.library()
    browser {
      commonWebpackConfig {
        // outputFileName = "app.js"
        // sourceMaps = true
        cssSupport { enabled = true }
      }

      testTask {
        enabled = true
        testLogging { configureLogEvents() }
        useKarma { useChromeHeadless() }
      }

      // distribution { outputDirectory = file("$projectDir/docs") }
    }
    compilations.configureEach { kotlinOptions { configureKotlinJs() } }
    testRuns.configureEach { executionTask.configure { configureTestReport() } }
  }

  sourceSets {
    jsMain {
      dependencies {
        api(libs.kotlinx.html)
        api(libs.ktor.client.js)
        api(kotlinw("browser"))
        api(kotlinw("css"))
        // implementation(npm("@js-joda/timezone", libs.versions.npm.jsjoda.tz.get()))
        // kspDependency("CommonMainMetadata", project(":meta:ksp:processor"))
        // kspDependency("Js", project(":meta:ksp:processor"))
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
    binaries.executable()
    browser {
      commonWebpackConfig {
        devServer =
            (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                // open = mapOf("app" to mapOf("name" to "google chrome")),
                static =
                    (devServer?.static ?: mutableListOf()).apply {
                      // Serve sources to debug inside browser
                      add(project.rootDir.path)
                    },
            )
      }
      applyBinaryen()
    }
  }
}

context(Project)
fun KotlinMultiplatformExtension.hostNativeTarget(
    configure: KotlinNativeTargetWithHostTests.() -> Unit = {}
) =
    when {
      Platform.isMac -> {
        macosX64 { configure() }
        macosArm64 { configure() }
      }
      Platform.isLinux -> {
        linuxX64 { configure() }
        // linuxArm64()
      }
      Platform.isWin -> mingwX64 { configure() }
      // targets.filter { it.platformType !in listOf(common, native) }.forEach {
      //    targets.remove(it)
      // }
      else ->
          throw GradleException(
              "Host OS '${Platform.currentOS}' is not supported in Kotlin/Native $project.")
    }

fun KotlinMultiplatformExtension.allNativeTargets(
    configure: KotlinNativeTargetWithHostTests.() -> Unit = {}
) {
  macosX64 { configure() }
  macosArm64 { configure() }
  linuxX64 { configure() }
  // linuxArm64 { configure() }
  // mingwX64 { configure() }
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
