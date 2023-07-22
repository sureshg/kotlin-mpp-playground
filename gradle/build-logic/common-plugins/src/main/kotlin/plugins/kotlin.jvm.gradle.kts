package plugins

import com.google.devtools.ksp.gradle.KspTaskJvm
import common.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-library`
  id("com.google.devtools.ksp")
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("kotlinx-atomicfu")
  id("dev.zacsweers.redacted")
  id("org.jetbrains.dokka")
  id("org.jetbrains.kotlinx.kover")
  // `test-suite-base`
}

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
  // Configure "compileJava" and "compileTestJava" tasks.
  withType<JavaCompile>().configureEach { configureJavac() }

  withType<KotlinCompile>().configureEach {
    usePreciseJavaTracking = true
    compilerOptions { configureKotlinJvm() }
    // finalizedBy("spotlessApply")
  }

  withType<JavaExec> { jvmArgs(jvmArguments()) }

  // configure jvm target for ksp
  withType(KspTaskJvm::class).all {
    compilerOptions { configureKotlinJvm() }
    jvmTargetValidationMode = JvmTargetValidationMode.WARNING
  }

  processResources {
    inputs.property("version", project.version.toString())
    filesMatching("*-res.txt") {
      expand(
          "name" to project.name,
          "version" to project.version,
      )
    }
  }

  // Javadoc
  javadoc {
    isFailOnError = true
    modularity.inferModulePath = true
    (options as StandardJavadocDocletOptions).apply {
      encoding = "UTF-8"
      linkSource(true)
      addBooleanOption("-enable-preview", true)
      addStringOption("-add-modules", addModules)
      addStringOption("-release", javaRelease.get().toString())
      addStringOption("Xdoclint:none", "-quiet")
    }
  }
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(kotlin("stdlib"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.atomicfu)
  // Auto-service
  ksp(libs.ksp.auto.service)
  implementation(libs.google.auto.annotations)
  // Test dependencies
  testImplementation(platform(libs.junit.bom))
  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlinx.lincheck)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.slf4j.simple)
  testImplementation(libs.mockk)
}
