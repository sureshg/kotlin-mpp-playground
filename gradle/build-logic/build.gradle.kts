@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.*

plugins {
  idea
  `kotlin-dsl`
  embeddedKotlin("plugin.serialization")
  alias(libs.plugins.jte)
  alias(libs.plugins.benmanes)
  alias(libs.plugins.spotless)
  alias(libs.plugins.autonomousapps.bestpractices)
  // alias(libs.plugins.kotlin.dsl)
}

// Java version used for Kotlin Gradle precompiled script plugins.
val dslJavaVersion = libs.versions.kotlin.dsl.jvmtarget

idea {
  module {
    isDownloadJavadoc = false
    isDownloadSources = false
  }
}

kotlin {
  compilerOptions {
    jvmTarget = dslJavaVersion.map(JvmTarget::fromTarget)
    freeCompilerArgs.addAll(
        "-Xcontext-receivers",
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
  val ktfmtVersion = libs.versions.ktfmt.get()
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

    // Re-exposure of plugin from dependency. Gradle doesn't expose the plugin itself.
    create("com.gradle.develocity") {
      id = "com.gradle.develocity"
      implementationClass = "com.gradle.develocity.agent.gradle.DevelocityPlugin"
      displayName = "Develocity Gradle Plugin"
      description = "Develocity gradle settings plugin re-exposed from dependency"
    }

    // A generic plugin for both project and settings
    register("Generic Plugin") {
      id = "plugins.generic"
      implementationClass = "plugins.GenericPlugin"
      displayName = "Generic plugin"
      description = "A plugin-aware pre-compiled generic plugin"
      tags = listOf("Generic Plugin", "build-logic")
    }

    // Dependency Reports plugin
    register("Dependency Reports") {
      id = "plugins.dependency.reports"
      implementationClass = "plugins.DepReportsPlugin"
      displayName = "Dependency Reports plugin"
      description = "A plugin to list all the resolved artifacts"
      tags = listOf("Dependency Reports", "build-logic")
    }

    // Uncomment the id to change plugin id for this pre-compiled plugin
    named("plugins.common") {
      // id = "build.plugins.common"
      displayName = "Common build-logic plugin"
      description = "Common pre-compiled script plugin"
      tags = listOf("Common Plugin", "build-logic")
    }

    // val settingsPlugin by registering {}
  }
}

// Jte is used for generating build config.
jte {
  contentType = gg.jte.ContentType.Plain
  sourceDirectory = sourceSets.main.map { it.resources.srcDirs.first().toPath() }
  generate()
  jteExtension("gg.jte.models.generator.ModelExtension") {
    property("language", "Kotlin")
    // property("interfaceAnnotation", "@foo.bar.MyAnnotation")
    // property("implementationAnnotation", "@foo.bar.MyAnnotation")
  }
  // jteExtension("gg.jte.nativeimage.NativeResourcesExtension")
  // binaryStaticContent = true
  // kotlinCompileArgs = arrayOf("-jvm-target", dslJavaVersion.get())
}

dependencies {
  // Hack to access version catalog from pre-compiled script plugins.
  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
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
  implementation(libs.build.dokka.plugin)
  implementation(libs.build.dokka.base)
  implementation(libs.build.redacted.plugin)
  implementation(libs.build.gradle.develocity)
  implementation(libs.build.nmcp.plugin)
  implementation(libs.build.nexus.plugin)
  implementation(libs.build.spotless.plugin)
  implementation(libs.build.shadow.plugin)
  implementation(libs.build.mrjar.plugin)
  implementation(libs.build.semver.plugin)
  implementation(libs.build.benmanesversions)
  implementation(libs.build.dependencyanalysis)
  implementation(libs.build.tasktree)
  implementation(libs.build.foojay.resolver)
  implementation(libs.build.nativeimage.plugin)
  implementation(libs.build.mokkery.plugin)
  implementation(libs.build.jte.plugin)
  implementation(libs.build.jib.plugin)
  implementation(libs.build.jib.nativeimage.extn)
  implementation(libs.build.github.changelog)
  implementation(libs.build.modulegraph.plugin)
  // implementation(libs.build.kopy.plugin)

  // implementation(libs.build.kotlin.compose.compiler)
  // implementation(libs.build.karakum.plugin)
  // implementation(libs.jte.native)
  // implementation(libs.build.kmp.hierarchy)
  // implementation(libs.build.includegit.plugin)
  // implementation(libs.build.cyclonedx.plugin)

  // For using kotlin-dsl in pre-compiled script plugins
  // implementation("${libs.build.kotlin.dsl.get().module}:${expectedKotlinDslPluginsVersion}")
  // testImplementation(gradleTestKit())
}
