package plugins

import com.google.devtools.ksp.gradle.KspAATask
import common.*
import java.util.jar.Attributes
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import tasks.BuildConfig
import tasks.BuildConfigExtension
import tasks.ReallyExecJar

plugins {
  java
  `kotlin-multiplatform`
  `kotlinx-serialization`
  com.google.devtools.ksp
  `kotlinx-atomicfu`
  dev.zacsweers.redacted
  id("plugins.kotlin.docs")
  // kotlin("plugin.power-assert")
  // org.gradle.kotlin.`kotlin-dsl`
  // app.cash.molecule
  // dev.mokkery
}

kotlin {
  commonTarget()
  when (project.name) {
    sharedProjectName -> {
      jvmTarget()
      jsTarget()
      // wasmJsTarget()
      allNativeTargets()
    }
    "js",
    "chrome",
    "web" -> jsTarget()
    "wasm" -> wasmJsTarget()
    "native" -> allNativeTargets()
    else -> jvmTarget()
  }

  applyDefaultHierarchyTemplate {
    common {
      group("jsCommon") {
        withJs()
        withWasmJs()
      }
    }
  }

  // kotlinDaemonJvmArgs = jvmArguments
  // explicitApiWarning()
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
  allWarningsAsErrors = false
}

atomicfu {
  transformJvm = true
  transformJs = true
  jvmVariant = "VH"
}

// powerAssert { functions = listOf("kotlin.require", "kotlin.check") }

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
  if (project.name == sharedProjectName) {
    val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
    val buildConfig by register<BuildConfig>("buildConfig", buildConfigExtn)
    kotlin.sourceSets.commonMain { kotlin.srcDirs(buildConfig) }
    // compileKotlinMetadata { dependsOn(buildConfig) }
    // maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
  }

  // Configure KSP2
  withType<KspAATask>().configureEach { configureKspConfig() }

  withType<KotlinJsCompile>().configureEach { kotlinOptions { configureKotlinJs() } }

  withType<KotlinNpmInstallTask>().configureEach { configureKotlinNpm() }

  withType<Jar>().configureEach {
    manifest {
      attributes(
          "Automatic-Module-Name" to project.group,
          "Built-By" to System.getProperty("user.name"),
          "Built-JDK" to System.getProperty("java.runtime.version"),
          "Built-OS" to System.getProperty("os.name"),
          "Created-By" to "Gradle ${gradle.gradleVersion}",
          Attributes.Name.IMPLEMENTATION_TITLE.toString() to project.name,
          Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version,
          Attributes.Name.IMPLEMENTATION_VENDOR.toString() to project.group,
      )
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
  }

  withType<ProcessResources>().configureEach {
    inputs.property("version", project.version.toString())
    filesMatching("*-res.txt") {
      expand(
          "name" to project.name,
          "version" to project.version,
      )
    }
  }

  plugins.withId("com.github.johnrengelman.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          jarFile = named<Jar>("shadowJar").flatMap { it.archiveFile }
          // javaOpts = application.applicationDefaultJvmArgs
          javaOpts = named<JavaExec>("run").get().jvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file("${project.name}-app") }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }
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

rootProject.plugins.run {
  var nodeExtnConfigured: String? by rootProject.extra

  withType<NodeJsRootPlugin> {
    rootProject.extensions.configure<NodeJsRootExtension> {
      download = true
      nodeExtnConfigured = "true"
      // version = libs.versions.node.version.get()
      // nodeDownloadBaseUrl = "https://nodejs.org/download/nightly"
    }
  }

  withType<YarnPlugin> {
    rootProject.extensions.configure<YarnRootExtension> {
      download = true
      lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
      yarnLockMismatchReport = YarnLockMismatchReport.WARNING
      yarnLockAutoReplace = false
      nodeExtnConfigured = "true"
    }
  }
}

dependencies {
  // add("kspJvm", project(":ksp-processor"))
}
