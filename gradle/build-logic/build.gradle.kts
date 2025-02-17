@file:Suppress("UnstableApiUsage")

import com.diffplug.spotless.kotlin.KtfmtStep
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
  idea
  `kotlin-dsl`
  embeddedKotlin("plugin.serialization")
  alias(libs.plugins.jte)
  alias(libs.plugins.benmanes)
  alias(libs.plugins.spotless)
  // alias(libs.plugins.autonomousapps.bestpractices)
  // alias(libs.plugins.kotlin.dsl)
}

// Java version used for Kotlin Gradle precompiled script plugins.
val dslJavaVersion = libs.versions.kotlin.dsl.jvmtarget

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget = dslJavaVersion.map(JvmTarget::fromTarget)
    freeCompilerArgs.addAll(
        "-Xjdk-release=${dslJavaVersion.get()}",
        "-Xno-param-assertions",
        "-Xno-call-assertions",
        "-Xno-receiver-assertions")
    optIn.addAll(
        "kotlin.ExperimentalStdlibApi",
        "kotlin.time.ExperimentalTime",
        "kotlin.io.encoding.ExperimentalEncodingApi",
        "kotlinx.validation.ExperimentalBCVApi",
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlinx.serialization.ExperimentalSerializationApi",
        "org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi",
        "org.jetbrains.kotlin.gradle.ExperimentalWasmDsl",
        "org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl",
        "org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDceDsl")
  }
}

spotless {
  val ktfmtVersion = maxOf(KtfmtStep.defaultVersion(), libs.versions.ktfmt.get())
  kotlin {
    target("src/**/*.kts", "src/**/*.kt")
    ktfmt(ktfmtVersion)
    trimTrailingWhitespace()
    endWithNewline()
  }

  kotlinGradle {
    target("*.kts")
    ktfmt(ktfmtVersion)
    trimTrailingWhitespace()
    endWithNewline()
  }
}

tasks {
  // Restrict the java release version used in Gradle kotlin DSL to avoid
  // accidentally using higher version JDK API in build scripts.
  compileJava {
    options.apply {
      release = dslJavaVersion.map { it.toInt() }
      isIncremental = true
    }
  }

  validatePlugins {
    failOnWarning = true
    enableStricterValidation = true
  }

  dependencyUpdates { checkConstraints = true }

  register("cleanAll") {
    description = "Cleans all projects"
    group = LifecycleBasePlugin.CLEAN_TASK_NAME
    allprojects.mapNotNull { it.tasks.findByName("clean") }.forEach { dependsOn(it) }
    // doLast { delete(layout.buildDirectory) }
  }
}

gradlePlugin {
  plugins {
    // Uncomment the id to change plugin id for this pre-compiled plugin
    named("dev.suresh.plugin.common") {
      // id = "${project.group}.${project.name}.common"
      displayName = "Common build-logic plugin"
      description = "Common pre-compiled script plugin"
      tags = listOf("Common Plugin", "build-logic")
    }

    // Dependency Reports plugin
    register("Dependency Reports") {
      id = "dev.suresh.plugin.depreports"
      implementationClass = "plugins.DepReportsPlugin"
      displayName = "Dependency Reports plugin"
      description = "A plugin to list all the resolved artifacts"
      tags = listOf("Dependency Reports", "build-logic")
    }

    // A generic plugin for both project and settings
    register("Generic Plugin") {
      id = "dev.suresh.plugin.generic"
      implementationClass = "plugins.GenericPlugin"
      displayName = "Generic plugin"
      description = "A plugin-aware pre-compiled generic plugin"
      tags = listOf("Generic Plugin", "build-logic")
    }

    // val settingsPlugin by registering {}
  }
}

// Jte is used for generating build config.
jte {
  contentType = gg.jte.ContentType.Plain
  generate()
  jteExtension("gg.jte.models.generator.ModelExtension") {
    property("language", "Kotlin")
    // property("interfaceAnnotation", "@foo.bar.MyAnnotation")
    // property("implementationAnnotation", "@foo.bar.MyAnnotation")
  }
  // sourceDirectory = sourceSets.main.map { it.resources.srcDirs.first().toPath() }
  // jteExtension("gg.jte.nativeimage.NativeResourcesExtension")
  // binaryStaticContent = true
  // kotlinCompileArgs = arrayOf("-jvm-target", dslJavaVersion.get())
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.ktor.client.java)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.encoding)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.resources)
  implementation(libs.ktor.client.auth)
  implementation(libs.ktor.serialization.json)
  implementation(libs.ajalt.mordant.coroutines)
  implementation(libs.build.zip.prefixer)
  implementation(libs.jte.runtime)
  jteGenerate(libs.jte.models)
  // compileOnly(libs.jte.kotlin)

  // External plugins deps to use in precompiled script plugins
  // https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins
  implementation(libs.build.kotlin)
  // OR implementation(kotlin("gradle-plugin"))
  implementation(libs.build.kotlin.ksp)
  implementation(libs.build.kotlinx.atomicfu)
  implementation(libs.build.kotlin.allopen)
  implementation(libs.build.kotlin.powerassert)
  implementation(libs.build.kotlin.jsplainobjects)
  implementation(libs.build.kotlinx.serialization)
  implementation(libs.build.kotlinx.kover)
  implementation(libs.build.kotlinx.benchmark)
  implementation(libs.build.kotlinx.bcv)
  implementation(libs.build.kmpmt)
  implementation(libs.build.dokka.plugin)
  implementation(libs.build.redacted.plugin)
  implementation(libs.build.gradle.develocity)
  implementation(libs.build.nmcp.plugin)
  implementation(libs.build.nexus.plugin)
  implementation(libs.build.spotless.plugin)
  implementation(libs.build.shadow.plugin)
  implementation(libs.build.mrjar.plugin)
  implementation(libs.build.semver.plugin)
  implementation(libs.build.benmanesversions)
  implementation(libs.build.tasktree)
  implementation(libs.build.foojay.resolver)
  implementation(libs.build.nativeimage.plugin)
  implementation(libs.build.mokkery.plugin)
  implementation(libs.build.jte.plugin)
  implementation(libs.build.jib.plugin)
  implementation(libs.build.jib.nativeimage.extn)
  implementation(libs.build.github.changelog)
  implementation(libs.build.modulegraph.plugin)
  implementation(libs.build.kopy.plugin)
  implementation(libs.build.tomlj)

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  // implementation(libs.build.kotlin.compose.compiler)
  // implementation(libs.build.karakum.plugin)
  // implementation(libs.jte.native)
  // implementation(libs.build.kmp.hierarchy)
  // implementation(libs.build.includegit.plugin)
  // implementation(libs.build.dependencyanalysis)

  testImplementation(gradleTestKit())
  // For using kotlin-dsl in pre-compiled script plugins
  // implementation("${libs.build.kotlin.dsl.get().module}:${expectedKotlinDslPluginsVersion}")
}
