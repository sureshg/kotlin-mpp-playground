package plugins

import common.*
import org.gradle.kotlin.dsl.*
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
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(kotlin("stdlib"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  // Auto-service
  ksp(libs.ksp.auto.service)
  implementation(libs.google.auto.annotations)
  // Test dependencies
  testImplementation(platform(libs.junit.bom))
  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.slf4j.simple)
}
