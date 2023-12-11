package plugins

import com.google.devtools.ksp.gradle.*
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
  com.bnorm.power.`kotlin-power-assert`
  id("plugins.kotlin.docs")
  // org.gradle.kotlin.`kotlin-dsl`
  // app.cash.molecule
  // dev.mokkery
}

kotlin {
  commonTarget()
  when (project.name) {
    commonProjectName -> {
      jvmTarget()
      jsTarget()
      // allNativeTargets()
      // wasmJsTarget()
    }
    "native" -> allNativeTargets()
    "web",
    "chrome" -> jsTarget()
    else -> jvmTarget()
  }
  applyDefaultHierarchyTemplate()
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
          Attributes.Name.IMPLEMENTATION_TITLE.toString() to project.name,
          Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version,
          Attributes.Name.IMPLEMENTATION_VENDOR.toString() to project.group,
      )
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
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
