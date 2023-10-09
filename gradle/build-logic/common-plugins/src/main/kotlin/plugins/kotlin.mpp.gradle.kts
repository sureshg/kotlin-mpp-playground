package plugins

import com.google.devtools.ksp.gradle.KspTaskJvm
import common.*
import java.util.jar.Attributes
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import tasks.BuildConfig
import tasks.BuildConfigExtension

plugins {
  java
  `kotlin-multiplatform`
  `kotlinx-serialization`
  com.google.devtools.ksp
  `kotlinx-atomicfu`
  dev.zacsweers.redacted
  com.bnorm.power.`kotlin-power-assert`
  id("plugins.kotlin.docs")
  // org.gradle.kotlin.`kotlin-dsl`
  // app.cash.molecule
  // dev.mokkery
}

kotlin {
  jvmToolchain { configureJvmToolchain() }
  withSourcesJar(publish = true)

  targets.all {
    // Configure all compilations of all targets
    compilations.all {
      compileTaskProvider.configure { compilerOptions { configureKotlinCommon() } }
      // compilerOptions.configure { configureKotlinCommon() }
    }
  }

  jvm {
    withJava()
    // withSourcesJar(publish = false)
    compilations.all {
      compileJavaTaskProvider?.configure { configureJavac() }
      compileTaskProvider.configure { compilerOptions { configureKotlinJvm() } }
      // compilerOptions.configure { configureKotlinJvm() }
    }

    // val test by testRuns.existing
    testRuns.configureEach { executionTask.configure { configureKotlinTest() } }
    // Attribute to distinguish JVM target
    attributes.attribute(mppTargetAttr, "jvm")
  }

  jvm("desktop") {
    compilations.all {
      compileJavaTaskProvider?.configure { configureJavac() }
      compileTaskProvider.configure { compilerOptions { configureKotlinJvm() } }
    }
    testRuns.configureEach { executionTask.configure { configureKotlinTest() } }
    // Attribute to distinguish Desktop target
    attributes.attribute(mppTargetAttr, "desktop")
  }

  js {
    useEsModules()
    binaries.executable()
    // binaries.library()
    browser {
      commonWebpackConfig {
        // outputFileName = "app.js"
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

  applyDefaultHierarchyTemplate()

  // Wasm and native targets are experimental.
  if (project.hasProperty("experimental")) {
    wasmJs {
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

  sourceSets {
    all {
      languageSettings { configureKotlinLang() }
      // Apply multiplatform library bom to all source sets
      dependencies {
        implementation(project.dependencies.platform(libs.kotlin.bom))
        implementation(project.dependencies.platform(libs.ktor.bom))
        implementation(project.dependencies.platform(libs.kotlin.wrappers.bom))
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
    //   dependsOn(commonMain)
    //   dependencies {
    //     implementation(..)
    //   }
    // }
    //

    // val target = targets.first { it.platformType == KotlinPlatformType.common }
    // val compilation = target.compilations["main"]
    // // OR val compilation = targets["metadata"].compilations["main"]
    // compilation.defaultSourceSet.kotlin.srcDir(buildConfig)
    // // val newSourceSet = sourceSets.create("gen")
    // // compilation.defaultSourceSet.dependsOn(newSourceSet)

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
        // https://kotlinlang.org/docs/ksp-multiplatform.html
        kspDependency("jvm", libs.ksp.auto.service)
      }
    }

    jvmTest {
      dependencies {
        implementation(project.dependencies.platform(libs.junit.bom))
        implementation(project.dependencies.platform(libs.testcontainers.bom))
        implementation(kotlin("test-junit5"))
        implementation(libs.slf4j.simple)
        implementation(libs.mockk)
        implementation(libs.testcontainers.junit5)
        implementation(libs.testcontainers.postgresql)
      }
    }

    jsMain {
      dependencies {
        api(libs.kotlinx.html)
        api(libs.ktor.client.js)
        api(kotlinw("browser"))
        // implementation(npm("@js-joda/timezone", libs.versions.npm.jsjoda.tz.get()))
        // kspDependency("CommonMainMetadata", project(":meta:ksp:processor"))
        // kspDependency("Js", project(":meta:ksp:processor"))
      }

      // kotlin.srcDir("src/main/kotlin")
      // resources.srcDir("src/main/resources")
    }

    jsTest { kotlin {} }
  }

  // kotlinDaemonJvmArgs = jvmArguments
  // explicitApiWarning()
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
}

atomicfu {
  transformJvm = true
  transformJs = true
  jvmVariant = "VH"
}

kotlinPowerAssert { functions = listOf("kotlin.assert", "kotlin.test.assertTrue") }

redacted {
  enabled = true
  replacementString = "█"
}

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
  // Register buildConfig task only for common module
  if (project.name == commonProjectName) {
    val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
    val buildConfig by register<BuildConfig>("buildConfig", buildConfigExtn)
    kotlin.sourceSets.commonMain { kotlin.srcDirs(buildConfig) }
    // compileKotlinMetadata { dependsOn(buildConfig) }
    // maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
  }

  // configure jvm target for ksp
  withType<KspTaskJvm>().all {
    compilerOptions { configureKotlinJvm() }
    jvmTargetValidationMode = JvmTargetValidationMode.WARNING
  }

  withType<KotlinJsCompile>().configureEach { kotlinOptions { configureKotlinJs() } }

  withType<KotlinNpmInstallTask>().configureEach { configureKotlinNpm() }

  withType<Jar>().configureEach {
    manifest {
      attributes(
          "Automatic-Module-Name" to project.group,
          "Built-By" to System.getProperty("user.name"),
          "Built-JDK" to System.getProperty("java.runtime.version"),
          Attributes.Name.IMPLEMENTATION_TITLE.toString() to project.name,
          Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version,
          Attributes.Name.IMPLEMENTATION_VENDOR.toString() to project.group,
      )
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
  }

  // Copy the js app to jvm resource
  named<Copy>("jvmProcessResources") {
    val jsBrowserDist = named("jsBrowserDistribution")
    from(jsBrowserDist)
    // filePermissions {}
    // dirPermissions {}
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

dependencies {
  // add("kspJvm", project(":ksp-processor"))
}

// A workaround to initialize Node.js and Yarn extensions only once in a multi-module
// project by setting extra properties on a root project from a subproject.
// https://docs.gradle.org/current/userguide/kotlin_dsl.html#extra_properties
var nodeExtnConfigured: String? by rootProject.extra

if (!nodeExtnConfigured.toBoolean()) {
  // https://kotlinlang.org/docs/js-project-setup.html#use-pre-installed-node-js
  rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.extensions.configure<NodeJsRootExtension> {
      download = true
      nodeVersion = libs.versions.node.version.get()
      nodeExtnConfigured = "true"
      // nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
    }
  }

  // https://kotlinlang.org/docs/js-project-setup.html#version-locking-via-kotlin-js-store
  rootProject.plugins.withType<YarnPlugin> {
    rootProject.extensions.configure<YarnRootExtension> {
      download = true
      lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      yarnLockMismatchReport = YarnLockMismatchReport.WARNING
      yarnLockAutoReplace = false
      nodeExtnConfigured = "true"
    }
  }
}

fun Project.addKspDependencyForAllTargets(
    dependencyNotation: Any,
    configurationNameSuffix: String = ""
) {
  // val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
  kotlin.targets
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
