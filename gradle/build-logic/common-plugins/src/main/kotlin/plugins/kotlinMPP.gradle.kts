package plugins

import dev.suresh.gradle.*
import dev.suresh.gradle.libs
import org.gradle.api.tasks.testing.logging.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.yarn.*

plugins {
  java
  kotlin("plugin.serialization")
  id("com.google.devtools.ksp")
  id("dev.zacsweers.redacted")
  id("org.jetbrains.dokka")
  id("org.jetbrains.kotlinx.kover")
}

// Workaround for "The root project is not yet available for build" error.
// https://slack-chats.kotlinlang.org/t/8236845/does-anybody-use-composite-builds-build-logic-with-applying-

apply(plugin = "org.jetbrains.kotlin.multiplatform")

val kotlinMPP = extensions.getByType<KotlinMultiplatformExtension>()

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlinMPP.apply {
  targetHierarchy.default()

  // Configure all compilations of all targets
  targets.all {
    compilations.all {
      compilerOptions.configure {
        apiVersion = kotlinApiVersion
        languageVersion = kotlinLangVersion
        allWarningsAsErrors = false
      }
    }
  }

  jvm {
    withJava()
    compilations.all {
      compileJavaTaskProvider?.configure {
        options.apply {
          encoding = "UTF-8"
          release = javaRelease
          isIncremental = true
          isFork = true
          debugOptions.debugLevel = "source,lines,vars"
          forkOptions.jvmArgs?.addAll(jvmArguments)
          compilerArgs.addAll(
              jvmArguments + listOf("-Xlint:all", "-parameters", "--add-modules=$addModules"))
        }
      }

      compilerOptions.configure {
        jvmTarget = kotlinJvmTarget
        verbose = true
        javaParameters = true
        suppressWarnings = false
        freeCompilerArgs.addAll(
            "-Xadd-modules=$addModules",
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            "-Xassertions=jvm",
            "-Xallow-result-return-type",
            "-Xemit-jvm-type-annotations",
            "-Xjspecify-annotations=strict",
        )
      }
    }

    // val test by testRuns.existing
    testRuns.configureEach {
      executionTask.configure {
        useJUnitPlatform()
        jvmArgs(jvmArguments)
        reports.html.required = true
        testLogging {
          events =
              setOf(
                  TestLogEvent.STANDARD_ERROR,
                  TestLogEvent.FAILED,
                  TestLogEvent.SKIPPED,
              )
          exceptionFormat = TestExceptionFormat.FULL
          showExceptions = true
          showCauses = true
          showStackTraces = true
          showStandardStreams = true
        }
      }
    }
  }
  // jvm("desktop") {}

  js(IR) {
    useEsModules()
    binaries.executable()
    browser {
      commonWebpackConfig {
        // outputFileName = "app.js"
        cssSupport { enabled.set(true) }
      }

      testTask {
        testLogging.showStandardStreams = true
        useKarma { useChromeHeadless() }
      }

      // @OptIn(ExperimentalDistributionDsl::class)
      // distribution { outputDirectory = file("$projectDir/docs") }
    }
  }

  //  wasm {
  //    binaries.executable()
  //    browser {
  //      commonWebpackConfig {
  //        devServer =
  //            (devServer ?: KotlinWebpackConfig.DevServer()).copy(
  //                open =
  //                    mapOf(
  //                        "app" to
  //                            mapOf(
  //                                "name" to "google chrome",
  //                            )),
  //            )
  //      }
  //    }
  //  }

  jvmToolchain {
    languageVersion = toolchainVersion
    vendor = toolchainVendor
  }

  @Suppress("UNUSED_VARIABLE")
  this.sourceSets {
    all {
      languageSettings.apply {
        progressiveMode = true
        optIn("kotlin.ExperimentalStdlibApi")
        optIn("kotlin.contracts.ExperimentalContracts")
        optIn("kotlin.ExperimentalUnsignedTypes")
        optIn("kotlin.io.path.ExperimentalPathApi")
        optIn("kotlin.time.ExperimentalTime")
        optIn("kotlin.ExperimentalMultiplatform")
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn("kotlinx.serialization.ExperimentalSerializationApi")
        optIn("kotlin.js.ExperimentalJsExport")
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }

    val jvmMain by getting { dependencies { implementation(libs.kotlin.stdlib.jdk8) } }
    val jvmTest by getting
    val jsMain by getting
    val jsTest by getting
  }
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
}

redacted { enabled = true }

kover {
  // useJacoco()
}

koverReport {
  defaults {
    filters { excludes {} }
    html { title = "${project.name} code coverage report" }
  }
}

tasks {
  // Copy the js app to jvm resource
  named<Copy>("jvmProcessResources") {
    val jsBrowserDist = named("jsBrowserDistribution")
    from(jsBrowserDist)
  }

  // Application run should use the jvmJar as classpath
  plugins.withId("application") {
    val jvmJar = named<Jar>("jvmJar")
    named<JavaExec>("run") {
      dependsOn(jvmJar)
      classpath(jvmJar)
    }
  }

  withType<KotlinJsCompile>().configureEach {
    kotlinOptions {
      // sourceMap = true
      // sourceMapEmbedSources = "always"
      // freeCompilerArgs += listOf("-Xir-per-module")
    }
  }
}

// A workaround to initialize Node.js and Yarn extensions only once in a multimodule project
val nodeConfigKey = "isNodeJSConfigured"
var isNodeJSConfigured = System.getProperty(nodeConfigKey).toBoolean()

if (!isNodeJSConfigured) {
  // https://kotlinlang.org/docs/js-project-setup.html#use-pre-installed-node-js
  rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.extensions.configure<NodeJsRootExtension> {
      download = true
      System.setProperty(nodeConfigKey, "true")
    }
  }
  // https://kotlinlang.org/docs/js-project-setup.html#version-locking-via-kotlin-js-store
  rootProject.plugins.withType<YarnPlugin> {
    rootProject.extensions.configure<YarnRootExtension> {
      download = true
      lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      System.setProperty(nodeConfigKey, "true")
    }
  }
}

// dependencies { add("kspJvm", project(":ksp-processor")) }
