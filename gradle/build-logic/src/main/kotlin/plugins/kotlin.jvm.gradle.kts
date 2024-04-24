package plugins

import com.google.devtools.ksp.gradle.KspAATask
import common.*
import java.util.jar.Attributes
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import tasks.ReallyExecJar

plugins {
  `java-library`
  com.google.devtools.ksp
  kotlin("jvm")
  `kotlinx-serialization`
  `kotlinx-atomicfu`
  dev.zacsweers.redacted
  id("plugins.kotlin.docs")
  kotlin("plugin.power-assert")
  // `test-suite-base`
}

// Apply the regular plugin
apply(plugin = "plugins.dependency.reports")

java {
  withSourcesJar()
  withJavadocJar()
  toolchain { configureJvmToolchain() }
}

kotlin {
  sourceSets.all {
    languageSettings { configureKotlinLang() }
    // kotlin.setSrcDirs(listOf("src/kotlin"))
  }
  jvmToolchain { configureJvmToolchain() }
}

@Suppress("UnstableApiUsage", "UNUSED_VARIABLE")
testing {
  suites {
    val test by
        getting(JvmTestSuite::class) {
          // OR "test"(JvmTestSuite::class) {}
          useJUnitJupiter(libs.versions.junit)
        }

    withType(JvmTestSuite::class) {
      // Configure all test suites
      targets.configureEach { testTask { configureJavaTest() } }
    }
  }
}

atomicfu {
  jvmVariant = "VH"
  transformJvm = true
  verbose = true
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
  allWarningsAsErrors = false
}

powerAssert {
  functions = listOf("kotlin.assert", "kotlin.test.assertTrue")
  excludedSourceSets = listOf("main")
}

redacted {
  enabled = true
  replacementString = "█"
}

kover {
  // useJacoco()
  reports {
    total {
      filters { excludes { classes("dev.suresh.example.*") } }
      html { title = "${project.name} code coverage report!" }
      verify {
        rule {
          minBound(0, CoverageUnit.LINE)
          minBound(0, CoverageUnit.BRANCH)
        }
        warningInsteadOfFailure = true
      }
    }
  }
}

// Java agent configuration for jib
val javaAgent by configurations.creating

tasks {
  // Configure "compileJava" and "compileTestJava" tasks.
  withType<JavaCompile>().configureEach {
    configureJavac()

    // Add the Kotlin classes to the module path
    options.compilerArgumentProviders.add(
        object : CommandLineArgumentProvider {
          @InputFiles
          @PathSensitive(PathSensitivity.RELATIVE)
          val kotlinClasses = compileKotlin.flatMap { it.destinationDirectory }

          override fun asArguments() =
              listOf("--patch-module", "$group=${kotlinClasses.get().asFile.absolutePath}")
        })
  }

  withType<KotlinCompile>().configureEach {
    usePreciseJavaTracking = true
    compilerOptions { configureKotlinJvm() }
    // finalizedBy("spotlessApply")
  }

  // Configure jvm args for JavaExec tasks except `run`
  withType<JavaExec>().matching { it.name != "run" }.configureEach { jvmArgs(jvmArguments()) }

  // Configure KSP2
  withType<KspAATask>().configureEach { configureKspConfig() }

  processResources {
    inputs.property("version", project.version.toString())
    filesMatching("*-res.txt") {
      expand(
          "name" to project.name,
          "version" to project.version,
      )
    }
  }

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

  plugins.withId("com.github.johnrengelman.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          val shadowJar = named<Jar>("shadowJar")
          jarFile = shadowJar.flatMap { it.archiveFile }
          // javaOpts = application.applicationDefaultJvmArgs
          javaOpts = named<JavaExec>("run").get().jvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file("${project.name}-app") }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }
  }

  // Copy OpenTelemetry Java agent for jib
  plugins.withId("com.google.cloud.tools.jib") {
    val copyOtelAgent by
        registering(Copy::class) {
          from(javaAgent)
          into(layout.buildDirectory.dir("otel"))
          rename { "otel-javaagent.jar" }
          duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

    processResources { dependsOn(copyOtelAgent) }
  }

  // Javadoc
  javadoc {
    isFailOnError = true
    (options as StandardJavadocDocletOptions).apply {
      encoding = "UTF-8"
      linkSource(true)
      addBooleanOption("-enable-preview", true)
      addStringOption("-add-modules", addModules)
      addStringOption("-release", javaRelease.get().toString())
      addStringOption("Xdoclint:none", "-quiet")
    }
    exclude("**/Main.java")
  }
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(platform(libs.kotlinx.coroutines.bom))
  implementation(platform(libs.kotlinx.serialization.bom))
  implementation(platform(libs.ktor.bom))
  implementation(platform(libs.kotlin.wrappers.bom))
  implementation(kotlin("stdlib"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.atomicfu)
  implementation(libs.kotlin.redacted.annotations)
  implementation(libs.jspecify)
  implementation(libs.password4j)
  implementation(libs.sslcontext.kickstart)
  // Auto-service
  ksp(libs.ksp.auto.service)
  implementation(libs.google.auto.annotations)

  // Test dependencies
  testImplementation(platform(libs.junit.bom))
  testImplementation(platform(libs.testcontainers.bom))
  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlinx.lincheck)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.slf4j.simple)
  testImplementation(libs.mockk)
  testImplementation(libs.testcontainers.junit5)
  testImplementation(libs.testcontainers.postgresql)
}

// Replace the standard jar with the one built by 'shadowJar' in both api and runtime variants
//
// configurations {
//   apiElements {
//     outgoing.artifacts.clear()
//     outgoing.artifact(shadowJar.flatMap {it.archiveFile})
//   }
//
//   runtimeElements {
//     outgoing.artifacts.clear()
//     outgoing.artifact(shadowJar.flatMap {it.archiveFile})
//   }
// }

allprojects {
  configurations.all {
    resolutionStrategy.eachDependency {
      if (requested.name.contains("intellij-coverage")) {
        // useVersion(libs.versions.intellij.coverage.get())
      }
    }
  }
}
