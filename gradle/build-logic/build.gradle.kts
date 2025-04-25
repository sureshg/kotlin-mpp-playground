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
    isDownloadJavadoc = false
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
        "org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl")
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
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ajalt.mordant.coroutines)
  implementation(libs.jte.runtime)
  jteGenerate(libs.jte.models)

  // External plugins deps to use in precompiled script plugins
  implementation(libs.plugins.kotlin.multiplatform.dep)
  implementation(libs.plugins.kotlin.allopen.dep)
  implementation(libs.plugins.kotlin.powerassert.dep)
  implementation(libs.plugins.kotlin.js.plainobjects.dep)
  implementation(libs.plugins.kotlin.ksp.dep)
  implementation(libs.plugins.kotlinx.atomicfu.dep)
  implementation(libs.plugins.kotlinx.serialization.dep)
  implementation(libs.plugins.kotlinx.kover.dep)
  implementation(libs.plugins.kotlinx.benchmark.dep)
  implementation(libs.plugins.kotlinx.bcv.dep)
  implementation(libs.plugins.jetbrains.dokka.dep)
  implementation(libs.plugins.graalvm.nativeimage.dep)
  implementation(libs.plugins.gradle.develocity.dep)
  implementation(libs.plugins.foojay.resolver.dep)
  implementation(libs.plugins.redacted.dep)
  implementation(libs.plugins.nmcp.dep)
  implementation(libs.plugins.spotless.dep)
  implementation(libs.plugins.shadow.dep)
  implementation(libs.plugins.mrjar.dep)
  implementation(libs.plugins.semver.dep)
  implementation(libs.plugins.benmanes.dep)
  implementation(libs.plugins.tasktree.dep)
  implementation(libs.plugins.mokkery.dep)
  implementation(libs.plugins.jte.dep)
  implementation(libs.plugins.jib.dep)
  implementation(libs.plugins.modulegraph.dep)
  implementation(libs.plugins.kopy.dep)
  implementation(libs.plugins.github.changelog.dep)

  implementation(libs.tomlj)
  implementation(libs.jib.nativeimage.extn)
  implementation(libs.zip.prefixer)
  implementation(libs.kmpmt)
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  testImplementation(gradleTestKit())
  // compileOnly(libs.jte.kotlin)
}

val Provider<PluginDependency>.dep: Provider<String>
  get() = map { "${it.module}:${it.version}" }

val PluginDependency.module
  get() = "$pluginId:$pluginId.gradle.plugin"
