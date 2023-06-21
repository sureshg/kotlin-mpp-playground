package plugins

import com.google.devtools.ksp.gradle.KspTaskJvm
import common.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import tasks.BuildConfig

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

val kotlinMultiplatform = extensions.getByType<KotlinMultiplatformExtension>()

kotlinMultiplatform.apply {
  targetHierarchy.default()

  jvmToolchain { configureJvmToolchain() }

  targets.all {
    // Configure all compilations of all targets
    compilations.all { compilerOptions.configure { configureKotlinCommon() } }
  }

  jvm {
    withJava()
    compilations.all {
      compileJavaTaskProvider?.configure { configureJavac() }
      compilerOptions.configure { configureKotlinJvm() }
    }

    // val test by testRuns.existing
    testRuns.configureEach { executionTask.configure { configureKotlinTest() } }
    attributes.attribute(mppTargetAttr, "jvm")
  }

  jvm("desktop") {
    compilations.all {
      compileJavaTaskProvider?.configure { configureJavac() }
      compilerOptions.configure { configureKotlinJvm() }
    }
    testRuns.configureEach { executionTask.configure { configureKotlinTest() } }
    attributes.attribute(mppTargetAttr, "desktop")
  }

  js(IR) {
    useEsModules()
    binaries.executable()
    browser {
      commonWebpackConfig {
        // outputFileName = "app.js"
        cssSupport { enabled.set(true) }
      }

      testTask {
        enabled = true
        testLogging.showStandardStreams = true
        useKarma { useChromeHeadless() }
      }

      // distribution { outputDirectory = file("$projectDir/docs") }
    }
  }

  // Disable wasm by default as some of the common dependencies are not compatible with wasm.
  if (project.hasProperty("wasm")) {
    wasm {
      binaries.executable()
      browser {
        commonWebpackConfig {
          devServer =
              (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                  open = mapOf("app" to mapOf("name" to "google chrome")))
        }
      }
    }
  }

  @Suppress("UNUSED_VARIABLE")
  this.sourceSets {
    all {
      languageSettings { configureKotlinLang() }
      // Apply multiplatform library bom to all source sets
      dependencies {
        implementation(project.dependencies.enforcedPlatform(libs.kotlin.bom))
        implementation(project.dependencies.enforcedPlatform(libs.ktor.bom))
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.client.serialization)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlin.stdlib.jdk8)
        // https://kotlinlang.org/docs/ksp-multiplatform.html
        project.dependencies.add("kspJvm", libs.ksp.auto.service)
        implementation(libs.google.auto.annotations)
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(project.dependencies.platform(libs.junit.bom))
        implementation(kotlin("test-junit5"))
      }
    }

    val jsMain by getting
    val jsTest by getting
  }

  // kotlinDaemonJvmArgs = jvmArguments
  // explicitApiWarning()
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

// https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects
val commonJsResources by
    configurations.creating {
      isCanBeConsumed = true
      isCanBeResolved = false
      attributes.attribute(Attribute.of("commonJSResources", String::class.java), "true")
    }

tasks {
  if (project.name == commonProjectName) {
    // Register buildConfig task only for common module
    val buildConfig by registering(BuildConfig::class) { classFqName = "BuildConfig" }
    kotlinMultiplatform.sourceSets.named("commonMain") { kotlin.srcDirs(buildConfig) }
    maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
  }

  // configure jvm target for ksp
  withType(KspTaskJvm::class).all {
    compilerOptions { configureKotlinJvm() }
    jvmTargetValidationMode = JvmTargetValidationMode.WARNING
  }

  withType<KotlinJsCompile>().configureEach { kotlinOptions { configureKotlinJs() } }

  withType<KotlinNpmInstallTask>().configureEach { configureKotlinNpm() }

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
}

artifacts { add("commonJsResources", tasks.named("jsProcessResources")) }

dependencies {
  // add("kspJvm", project(":ksp-processor"))
}

// A workaround to initialize Node.js and Yarn extensions only once in a multimodule
// project by setting extra properties on a root project from a subproject.
// https://docs.gradle.org/current/userguide/kotlin_dsl.html#extra_properties
var isNodeJSConfigured: String? by rootProject.extra

if (!isNodeJSConfigured.toBoolean()) {
  // https://kotlinlang.org/docs/js-project-setup.html#use-pre-installed-node-js
  rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.extensions.configure<NodeJsRootExtension> {
      download = true
      isNodeJSConfigured = "true"
      // nodeVersion = "20.0.0-v8-canaryxxxx"
      // nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
    }
  }

  // https://kotlinlang.org/docs/js-project-setup.html#version-locking-via-kotlin-js-store
  rootProject.plugins.withType<YarnPlugin> {
    rootProject.extensions.configure<YarnRootExtension> {
      download = true
      lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      isNodeJSConfigured = "true"
    }
  }
}
