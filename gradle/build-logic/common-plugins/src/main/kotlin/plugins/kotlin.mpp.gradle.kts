package plugins

import com.google.devtools.ksp.gradle.KspTaskJvm
import common.*
import java.util.jar.Attributes
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
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
  id("plugins.kotlin.docs")
  // app.cash.molecule
  // dev.petuska.npm.publish
  // dev.mokkery
}

kotlin {
  // applyDefaultHierarchyTemplate()
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

  // Disable wasm by default as some of the common dependencies are not compatible with wasm.
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

    // Use custom allocator for native targets
    macosX64("native") {
      binaries.executable()
      compilations.configureEach {
        compilerOptions.configure { freeCompilerArgs.add("-Xallocator=custom") }
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

    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.datetime)
        api(libs.kotlinx.atomicfu)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.collections.immutable)
        api(libs.kotlin.redacted.annotations)
        api(libs.parsus)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.cash.turbine)
      }
    }

    // val jvmCommon by creating {
    //   dependsOn(commonMain)
    //   dependencies {
    //     implementation(..)
    //   }
    // }

    val jvmMain by getting {
      // dependsOn(jvmCommon)
      dependencies {
        implementation(libs.kotlin.stdlib.jdk8)
        implementation(libs.kotlin.reflect)
        implementation(libs.google.auto.annotations)
        implementation(libs.slf4j.api)
        // https://kotlinlang.org/docs/ksp-multiplatform.html
        kspDependency("jvm", libs.ksp.auto.service)
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(project.dependencies.platform(libs.junit.bom))
        implementation(kotlin("test-junit5"))
        implementation(libs.slf4j.simple)
        implementation(libs.mockk)
      }
    }

    val jsMain by getting {
      dependencies {
        implementation(kotlinw("browser"))
        // kspDependency("CommonMainMetadata", project(":meta:ksp:processor"))
        // kspDependency("Js", project(":meta:ksp:processor"))
      }

      // kotlin.srcDir("src/main/kotlin")
      // resources.srcDir("src/main/resources")
    }

    val jsTest by getting
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

// https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:simple-sharing-artifacts-between-projects
val commonJsResources by
    configurations.creating {
      isCanBeConsumed = true
      isCanBeResolved = false
      attributes.attribute(Attribute.of("commonJSResources", String::class.java), "true")
    }

tasks {
  // Register buildConfig task only for common module
  if (project.name == commonProjectName) {
    val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
    val buildConfig by register<BuildConfig>("buildConfig", buildConfigExtn)
    kotlin.sourceSets.named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
      kotlin.srcDirs(buildConfig)
    }
    // maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
  }

  // configure jvm target for ksp
  withType(KspTaskJvm::class).all {
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

artifacts { add(commonJsResources.name, tasks.named("jsProcessResources")) }

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
